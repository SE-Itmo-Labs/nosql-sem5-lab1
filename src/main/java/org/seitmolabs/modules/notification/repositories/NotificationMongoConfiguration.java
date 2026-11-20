package org.seitmolabs.modules.notification.repositories;

import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.BsonType;
import org.bson.Document;
import org.seitmolabs.modules.notification.domain.Notification;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMongoConfiguration implements BeforeConvertCallback<Notification>, InitializingBean {

    private static final String SEQUENCES = "sequences";

    private final MongoDatabaseFactory databaseFactory;

    @Override
    public void afterPropertiesSet() {
        var database = databaseFactory.getMongoDatabase();
        var notifications = database.getCollection(Notification.COLLECTION_NAME);
        notifications.createIndex(Indexes.ascending("sourceEventId"), new IndexOptions()
                .name("sourceEventId")
                .unique(true)
                .partialFilterExpression(Filters.type("sourceEventId", "string")));
        ensureCategorySearchIndex(notifications);

        // Backfill the sort and filter field once per legacy notification.
        notifications.updateMany(
                Filters.and(Filters.eq("updatedAt", null), Filters.type("createdAt", BsonType.DATE_TIME)),
                List.of(new Document("$set", new Document("updatedAt", "$createdAt"))));

        // Resume above existing numeric IDs without moving a previously used counter backwards.
        var lastNotification = notifications
                .find(Filters.or(Filters.type("_id", BsonType.INT32), Filters.type("_id", BsonType.INT64)))
                .sort(Sorts.descending("_id"))
                .projection(Projections.include("_id"))
                .first();
        long lastId = lastNotification == null ? 0L : lastNotification.get("_id", Number.class).longValue();
        database.getCollection(SEQUENCES).updateOne(Filters.eq("_id", Notification.COLLECTION_NAME),
                Updates.max("value", lastId), new UpdateOptions().upsert(true));
    }

    public static void ensureCategorySearchIndex(MongoCollection<Document> notifications) {
        notifications.createIndex(Indexes.compoundIndex(
                Indexes.ascending("userId", "categoryId"),
                Indexes.descending("createdAt", "_id")),
                new IndexOptions().name("notifications_user_category_created_id"));
    }

    @Override
    public Notification onBeforeConvert(Notification notification, String collection) {
        if (notification.getId() != null) {
            return notification;
        }

        var sequence = databaseFactory.getMongoDatabase().getCollection(SEQUENCES).findOneAndUpdate(
                Filters.eq("_id", Notification.COLLECTION_NAME), Updates.inc("value", 1L),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
        if (sequence == null || !(sequence.get("value") instanceof Number value)) {
            throw new IllegalStateException("Не удалось получить ID уведомления");
        }
        notification.setId(value.longValue());
        return notification;
    }
}
