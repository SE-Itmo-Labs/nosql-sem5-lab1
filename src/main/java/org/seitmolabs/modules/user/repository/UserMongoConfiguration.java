package org.seitmolabs.modules.user.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

/** Configures user identity before demonstration accounts are created. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserMongoConfiguration implements BeforeConvertCallback<User>, ApplicationRunner {
    private final MongoDatabaseFactory databaseFactory;

    public UserMongoConfiguration(MongoDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public User onBeforeConvert(User user, String collection) {
        if (user.getId() == null) {
            Document sequence = databaseFactory.getMongoDatabase().getCollection("sequences")
                    .findOneAndUpdate(Filters.eq("_id", "users"), Updates.inc("value", 1L),
                            new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
            if (sequence == null || !(sequence.get("value") instanceof Number number)) {
                throw new IllegalStateException("Не удалось получить ID пользователя");
            }
            user.setId(number.longValue());
        }
        return user;
    }

    @Override
    public void run(ApplicationArguments args) {
        databaseFactory.getMongoDatabase().getCollection("users").createIndex(
                Indexes.ascending("username"),
                new IndexOptions().name("uk_users_username").unique(true));
    }
}
