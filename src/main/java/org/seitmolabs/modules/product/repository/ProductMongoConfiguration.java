package org.seitmolabs.modules.product.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.seitmolabs.modules.product.domain.Product;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProductMongoConfiguration implements BeforeConvertCallback<Product>, ApplicationRunner {
    private final MongoDatabaseFactory databaseFactory;

    public ProductMongoConfiguration(MongoDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public Product onBeforeConvert(Product product, String collection) {
        if (product.getId() == null) {
            Document sequence = databaseFactory.getMongoDatabase().getCollection("sequences")
                    .findOneAndUpdate(Filters.eq("_id", "products"), Updates.inc("value", 1L),
                            new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
            if (sequence == null || !(sequence.get("value") instanceof Number number)) {
                throw new IllegalStateException("Не удалось получить ID товара");
            }
            product.setId(number.longValue());
        }
        return product;
    }

    @Override
    public void run(ApplicationArguments args) {
        databaseFactory.getMongoDatabase().getCollection("products").createIndex(
                Indexes.ascending("name"), new IndexOptions().name("uk_products_name").unique(true));
    }
}
