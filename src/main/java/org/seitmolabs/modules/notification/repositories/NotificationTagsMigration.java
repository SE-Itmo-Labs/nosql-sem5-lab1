package org.seitmolabs.modules.notification.repositories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.seitmolabs.modules.notification.domain.Notification;

final class NotificationTagsMigration {

    private static final int VERSION = Notification.TAGS_SCHEMA_VERSION;
    private static final Document STRING_ITEMS = new Document("bsonType", "string");
    private static final Document MIGRATION_INPUT = new Document("bsonType", "object")
            .append("properties", new Document("tag", new Document("bsonType", List.of("string", "null")))
                    .append("tags", new Document("bsonType", List.of("array", "null")).append("items", STRING_ITEMS))
                    .append("schemaVersion", new Document("bsonType", List.of("int", "long", "null"))
                            .append("enum", Arrays.asList(null, 1, VERSION))));
    private static final Document CURRENT_SCHEMA = new Document("bsonType", "object")
            .append("required", List.of("tags", "schemaVersion"))
            .append("properties", new Document("tags", new Document("bsonType", "array").append("items", STRING_ITEMS))
                    .append("schemaVersion", new Document("bsonType", List.of("int", "long"))
                            .append("enum", List.of(VERSION))));

    private NotificationTagsMigration() {
    }

    static long migrate(MongoCollection<Document> collection) {
        var malformed = collection.find(Filters.nor(Filters.jsonSchema(MIGRATION_INPUT)))
                .projection(Projections.include("_id")).limit(5)
                .map(document -> document.get("_id")).into(new ArrayList<>());
        if (!malformed.isEmpty()) {
            throw new IllegalStateException("Некорректный формат меток или версии уведомлений с ID " + malformed);
        }

        Bson outdated = Filters.or(Filters.exists("tag"), Filters.eq("tags", null),
                Filters.ne("schemaVersion", VERSION));
        // Keep the order of the current array and only append a nonempty, new legacy tag.
        var merge = Document.parse("""
                { "$let": {
                  "vars": {
                    "existing": { "$ifNull": ["$tags", []] },
                    "legacy": { "$trim": { "input": { "$ifNull": ["$tag", ""] } } }
                  },
                  "in": { "$cond": [
                    { "$or": [{ "$eq": ["$$legacy", ""] }, { "$in": ["$$legacy", "$$existing"] }] },
                    "$$existing",
                    { "$concatArrays": ["$$existing", ["$$legacy"]] }
                  ] }
                } }
                """);
        return collection.updateMany(Filters.and(outdated, Filters.jsonSchema(MIGRATION_INPUT)), List.of(
                new Document("$set", new Document("tags", merge).append("schemaVersion", VERSION)),
                new Document("$unset", "tag"))).getModifiedCount();
    }

    static void installValidator(MongoDatabase database) {
        database.runCommand(new Document("collMod", Notification.COLLECTION_NAME)
                .append("validator", new Document("$and", List.of(
                        new Document("$jsonSchema", CURRENT_SCHEMA),
                        new Document("tag", new Document("$exists", false)))))
                .append("validationLevel", "strict")
                .append("validationAction", "error"));
    }
}
