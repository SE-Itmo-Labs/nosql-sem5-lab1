package org.seitmolabs.modules.categories.repositories;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.RequiredArgsConstructor;
import org.bson.BsonType;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.domain.CategoryDocument;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/** Preserves category uniqueness constraints in MongoDB. */
@Component
@Order(0)
@RequiredArgsConstructor
public class CategoryIndexesInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final CategoryIdGenerator idGenerator;
    private final CategoryCacheRepository cacheRepository;

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

        // System notifications need this category even with demo data disabled.
        Query systemCategory = Query.query(Criteria.where("systemCode").is(Category.BOOKING_CODE));
        if (mongoTemplate.exists(systemCategory, CategoryDocument.class)) {
            return;
        }
        CategoryDocument category = mongoTemplate.findAndModify(
                Query.query(Criteria.where("name").is("Бронирование")),
                new Update().set("systemCode", Category.BOOKING_CODE)
                        .setOnInsert("id", idGenerator.nextId())
                        .setOnInsert("description", "Изменения состояния билета"),
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                CategoryDocument.class);
        if (category != null) {
            cacheRepository.delete(category.getId());
            cacheRepository.deleteList();
        }
    }
}
