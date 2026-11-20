package org.seitmolabs.modules.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.domain.ProcessingRule;
import org.seitmolabs.modules.notification.dto.NotificationSearchPage;
import org.seitmolabs.modules.notification.dto.NotificationSearchRequest;
import org.seitmolabs.modules.notification.dto.NotificationSearchResult;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.repositories.MongoNotificationRepository;
import org.seitmolabs.modules.notification.repositories.NotificationSearchCache;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final NotificationSearchCache searchCache;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Notification create(CreateNotificationRequest request, String actorId) {
        if (userRepository.findByUsername(request.userId()).isEmpty()) {
            throw new ResourceNotFoundException("Пользователь не найден: " + request.userId());
        }

        return inTransaction(() -> {
            Notification inserted = notificationRepository.insert(notificationFactory.createSent(request, actorId));
            searchCache.invalidate(inserted.getUserId());
            return inserted;
        });
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

//    @Override
//    public List<Notification> getByUserId(String userId) {
//        return notificationRepository.findIdsByUserId(userId).stream()
//                .map(notificationRepository::findById)
//                .flatMap(Optional::stream)
//                .toList();
//    }

    @Override
    public List<Notification> getByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public NotificationSearchResult search(String userId, NotificationSearchRequest request) {
        validateRange(request.updatedFrom(), request.updatedTo(), "updatedFrom", "updatedTo");
        validateRange(request.createdFrom(), request.createdTo(), "createdFrom", "createdTo");
        var normalized = request.normalized();
        return searchCache.getOrLoad(userId, normalized, () -> loadSearchPage(userId, normalized));
    }

    private NotificationSearchPage loadSearchPage(String userId, NotificationSearchRequest request) {
        int page = request.page();
        int size = request.size();
        var pagination = PageRequest.of(page, size, request.sort().toSort());
        Query query = searchQuery(userId, request).withReadPreference(ReadPreference.primary());
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
        if (request.q() != null) {
            Pattern text = Pattern.compile(Pattern.quote(request.q()),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            criteria.orOperator(Criteria.where("title").regex(text), Criteria.where("text").regex(text));
        }
        addDateRange(criteria, "updatedAt", request.updatedFrom(), request.updatedTo());
        addDateRange(criteria, "createdAt", request.createdFrom(), request.createdTo());
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

    private void validateRange(Instant from, Instant to, String fromParameter, String toParameter) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new BadRequestException(fromParameter + " должен быть раньше " + toParameter);
        }
    }

    private void addDateRange(Criteria criteria, String field, Instant from, Instant to) {
        if (from == null && to == null) {
            return;
        }
        Criteria range = criteria.and(field);
        if (from != null) {
            range.gte(from);
        }
        if (to != null) {
            range.lt(to);
        }
    }

    @Override
    public Notification markRead(Long id, String userId) {
        return inTransaction(() -> markReadInTransaction(id, userId));
    }

    private Notification markReadInTransaction(Long id, String userId) {
        Query unreadNotification = Query.query(Criteria.where("id").is(id)
                .and("userId").is(userId).and("status").ne(NotificationStatus.READ));
        NotificationAction action = new NotificationAction(
                NotificationAction.Type.READ, userId, Instant.now(), null);
        Update update = new Update().set("status", NotificationStatus.READ);
        Notification updated = updateNotification(unreadNotification, update, action);
        if (updated == null) {
            return getByIdForUser(id, userId);
        }
        searchCache.invalidate(userId);
        return updated;
    }

    @Override
    public Notification applyRule(Long id, Long ruleId, String actorId) {
        return inTransaction(() -> applyRuleInTransaction(id, ruleId, actorId));
    }

    private Notification applyRuleInTransaction(Long id, Long ruleId, String actorId) {
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
            // Count only applications that added a journal entry, in the same MongoDB transaction.
            var ruleUpdate = mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(ruleId)),
                    new Update().inc("appliedCount", 1L).max("lastAppliedAt", action.at()),
                    ProcessingRule.class);
            if (ruleUpdate.getMatchedCount() != 1) {
                throw new ResourceNotFoundException("Правило обработки не найдено: " + ruleId);
            }
            searchCache.invalidate(updated.getUserId());
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
        inTransaction(() -> {
            notificationRepository.delete(getByIdForUser(id, userId));
            searchCache.invalidate(userId);
            return null;
        });
    }

    @Override
    public void deliver(OutboxEvent event) {
        Long categoryId = categoryRepository.findBySystemCode(Category.BOOKING_CODE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Системная категория уведомлений не найдена: " + Category.BOOKING_CODE))
                .getId();
        try {
            inTransaction(() -> {
                Notification notification = notificationFactory.createSystem(event, categoryId);
                notificationRepository.insert(notification);
                searchCache.invalidate(notification.getUserId());
                return null;
            });
        } catch (DuplicateKeyException exception) {
            // A duplicate insert aborts the transaction. Confirm the existing delivery afterwards.
            if (!notificationRepository.existsBySourceEventId(event.getId().toString())) {
                throw exception;
            }
        }
    }

    private <T> T inTransaction(Supplier<T> operation) {
        for (int attempt = 1; ; attempt++) {
            try {
                return transactionTemplate.execute(status -> operation.get());
            } catch (RuntimeException exception) {
                if (attempt >= 5 || !isRetryable(exception)) {
                    throw exception;
                }
            }
        }
    }

    private boolean isRetryable(Throwable failure) {
        if (failure instanceof OptimisticLockingFailureException) {
            return true;
        }
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof MongoException mongoException) {
                return mongoException.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)
                        && !mongoException.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL);
            }
        }
        return false;
    }
}
