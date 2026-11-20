package org.seitmolabs.demo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.seitmolabs.modules.notification.repositories.NotificationMongoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;

@Order(70)
@RequiredArgsConstructor
class NotificationIndexDemo implements DemoScenario {
    private static final String COLLECTION = "notifications_explain_demo";
    private static final Date FROM = Date.from(Instant.parse("2026-11-01T00:00:00Z"));
    private static final Date TO = Date.from(Instant.parse("2026-11-08T00:00:00Z"));
    private static final Date RECENT = Date.from(Instant.parse("2026-11-07T12:00:00Z"));

    private final MongoTemplate mongoTemplate;

    @Override
    public void run() {
        // Keep benchmark records out of the application's notification history.
        var notifications = mongoTemplate.getCollection(COLLECTION);
        NotificationMongoConfiguration.ensureCategorySearchIndex(notifications);
        NotificationMongoConfiguration.ensureRuleSearchIndex(notifications);

        var writes = new ArrayList<UpdateOneModel<Document>>();
        for (long id = 1; id <= 96; id++) {
            boolean ownNotification = id % 2 == 0;
            writes.add(example(id, ownNotification ? "user" : "other-user",
                    id % 3 == 0 ? 8L : 7L, ownNotification ? TO : RECENT));
        }
        writes.addAll(List.of(
                example(5_001L, "user", 7L, FROM),
                example(5_002L, "user", 7L, RECENT),
                example(5_003L, "user", 7L, RECENT),
                example(5_004L, "user", 8L, RECENT)));

        notifications.bulkWrite(writes, new BulkWriteOptions().ordered(false));

        // Remove surplus fixtures from the previous version of this demo.
        notifications.deleteMany(Filters.and(
                Filters.gt("_id", 96L),
                Filters.lte("_id", 5_000L),
                Filters.regex("title", "^\\[Демо\\] Уведомление [0-9]+$")));
    }

    private UpdateOneModel<Document> example(long id, String userId, long categoryId, Date createdAt) {
        return new UpdateOneModel<>(Filters.eq("_id", id),
                new Document("$setOnInsert", new Document("userId", userId)
                        .append("categoryId", categoryId)
                        .append("createdAt", createdAt)
                        .append("title", "[Демо] Уведомление " + id))
                        .append("$set", new Document("processingRuleId", categoryId).append("updatedAt", createdAt)),
                new UpdateOptions().upsert(true));
    }
}
