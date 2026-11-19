package org.seitmolabs.modules.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.domain.ProcessingRule;
import org.seitmolabs.modules.notification.dto.NotificationSearchPage;
import org.seitmolabs.modules.notification.dto.NotificationSearchRequest;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.repositories.MongoNotificationRepository;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import org.seitmolabs.common.exceptions.BadRequestException;
import org.seitmolabs.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MongoNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationFactory notificationFactory;
    private final CategoryDbRepository categoryRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Notification create(CreateNotificationRequest request, String actorId) {
        if (userRepository.findByUsername(request.userId()).isEmpty()) {
            throw new ResourceNotFoundException("Пользователь не найден: " + request.userId());
        }

        Notification notification = notificationFactory.createSent(request, actorId);
        return notificationRepository.insert(notification);
    }

    @Override
    public Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Уведомление не найдено: " + id));
    }

    @Override
    public Notification getByIdForUser(Long id, String userId) {
        Notification notification = getById(id);
        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Уведомление не найдено: " + id);
        }
        return notification;
    }

    @Override
    public List<Notification> getByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public NotificationSearchPage search(String userId, NotificationSearchRequest request) {
        if (request.updatedFrom() != null && request.updatedTo() != null
                && !request.updatedFrom().isBefore(request.updatedTo())) {
            throw new BadRequestException("updatedFrom должен быть раньше updatedTo");
        }

        int page = Objects.requireNonNullElse(request.page(), 0);
        int size = Objects.requireNonNullElse(request.size(), 20);
        var order = Objects.requireNonNullElse(request.sort(), NotificationSearchRequest.Order.UPDATED_AT_DESC);
        var pagination = PageRequest.of(page, size, order.toSort());
        Query query = searchQuery(userId, request);
        long totalElements = mongoTemplate.count(query, Notification.class);

        // The journal participates in matching, but is internal and need not be loaded for the response.
        query.fields().exclude("actionLog");
        var items = mongoTemplate.find(query.with(pagination), Notification.class).stream()
                .map(NotificationSearchPage.Item::from)
                .toList();
        return new NotificationSearchPage(items, page, size, totalElements, Math.ceilDiv(totalElements, size));
    }

    private Query searchQuery(String userId, NotificationSearchRequest request) {
        Criteria criteria = Criteria.where("userId").is(userId);
        if (request.processingRuleId() != null) {
            criteria.and("processingRuleId").is(request.processingRuleId());
        }
        if (request.status() != null) {
            criteria.and("status").is(request.status());
        }
        if (request.categoryId() != null) {
            criteria.and("categoryId").is(request.categoryId());
        }
        if (request.tag() != null) {
            criteria.and("tags").is(request.tag());
        }
        if (request.q() != null && !request.q().isBlank()) {
            Pattern text = Pattern.compile(Pattern.quote(request.q().strip()),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            criteria.orOperator(Criteria.where("title").regex(text), Criteria.where("text").regex(text));
        }
        if (request.updatedFrom() != null || request.updatedTo() != null) {
            Criteria updatedAt = criteria.and("updatedAt");
            if (request.updatedFrom() != null) {
                updatedAt.gte(request.updatedFrom());
            }
            if (request.updatedTo() != null) {
                updatedAt.lt(request.updatedTo());
            }
        }
        if (request.actionType() != null || request.actorId() != null) {
            // One journal entry must satisfy both the action type and the actor filters.
            Criteria entry = new Criteria();
            if (request.actionType() != null) {
                entry.and("type").is(request.actionType().name());
            }
            if (request.actorId() != null) {
                entry.and("actorId").is(request.actorId());
            }
            criteria.and("actionLog").elemMatch(entry);
        }
        return Query.query(criteria);
    }

    @Override
    public Notification markRead(Long id, String userId) {
        Query unreadNotification = Query.query(Criteria.where("id").is(id)
                .and("userId").is(userId).and("status").ne(NotificationStatus.READ));
        NotificationAction action = new NotificationAction(
                NotificationAction.Type.READ, userId, Instant.now(), null);
        Update update = new Update().set("status", NotificationStatus.READ);
        Notification updated = updateNotification(unreadNotification, update, action);
        return updated != null ? updated : getByIdForUser(id, userId);
    }

    @Override
    public Notification applyRule(Long id, Long ruleId, String actorId) {
        ProcessingRule rule = mongoTemplate.findById(ruleId, ProcessingRule.class);
        if (rule == null) {
            throw new ResourceNotFoundException("Правило обработки не найдено: " + ruleId);
        }

        Query matchingNotification = Query.query(Criteria.where("id").is(id)
                .and("sourceEventType").is(rule.eventType()).and("processingRuleId").ne(ruleId));
        NotificationAction action = new NotificationAction(
                NotificationAction.Type.RULE_APPLIED, actorId, Instant.now(), ruleId);
        Update update = new Update().set("processingRuleId", ruleId);
        update.addToSet("tags").each(rule.tagsToAdd().toArray());

        Notification updated = updateNotification(matchingNotification, update, action);
        if (updated != null) {
            return updated;
        }

        Notification notification = getById(id);
        if (notification.getSourceEventType() != rule.eventType()) {
            throw new BadRequestException("Правило не подходит для уведомления: " + id);
        }
        return notification;
    }

    private Notification updateNotification(Query query, Update update, NotificationAction action) {
        // Change the notification and append its journal entry in one document update.
        update.push("actionLog", action).max("updatedAt", action.at());
        return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true),
                Notification.class);
    }

    @Override
    public void delete(Long id, String userId) {
        notificationRepository.delete(getByIdForUser(id, userId));
    }

    @Override
    public void deliver(OutboxEvent event) {
        Long categoryId = categoryRepository.findBySystemCode(Category.BOOKING_CODE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Системная категория уведомлений не найдена: " + Category.BOOKING_CODE))
                .getId();
        Notification notification = notificationFactory.createSystem(
                event, categoryId);
        notificationRepository.saveSystem(notification);
    }
}
