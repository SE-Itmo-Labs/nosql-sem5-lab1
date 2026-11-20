package org.seitmolabs.modules.categories.repositories;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class CategoryIdGenerator {
    private static final String SEQUENCE_COLLECTION = "sequences";
    private static final String CATEGORY_SEQUENCE = "categories";

    private final MongoTemplate mongoTemplate;

    public long nextId() {
        Query query = Query.query(
                Criteria.where("_id").is(CATEGORY_SEQUENCE)
        );

        Update update = new Update().inc("value", 1L);

        Document seq = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options()
                        .upsert(true)
                        .returnNew(true),
                Document.class,
                SEQUENCE_COLLECTION
        );

        Number value = Objects.requireNonNull(
                seq,
                "MongoDB не вернула счётчик категорий"
        ).get("value", Number.class);

        return Objects.requireNonNull(
                value,
                "В счётчике категорий отсутствует значение"
        ).longValue();
    }
}
