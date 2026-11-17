package org.seitmolabs.modules.categories.repositories;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.RequiredArgsConstructor;
import org.bson.BsonType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/** Preserves category uniqueness constraints in MongoDB. */
@Component
@RequiredArgsConstructor
public class CategoryIndexesInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        var collection = mongoTemplate.getCollection("categories");

        collection.createIndex(
                Indexes.ascending("name"),
                new IndexOptions().name("uk_categories_name").unique(true)
        );
        collection.createIndex(
                Indexes.ascending("systemCode"),
                new IndexOptions()
                        .name("uk_categories_system_code")
                        .unique(true)
                        .partialFilterExpression(Filters.type("systemCode", BsonType.STRING))
        );
    }
}
