package org.seitmolabs.modules.catalog.config;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.seitmolabs.modules.catalog.auditorium.domain.Auditorium;
import org.seitmolabs.modules.catalog.movie.domain.Movie;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class CatalogMongoConfiguration implements BeforeConvertCallback<Object>, ApplicationRunner {
    private final MongoDatabaseFactory databaseFactory;

    public CatalogMongoConfiguration(MongoDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public Object onBeforeConvert(Object entity, String collection) {
        // Allocate identifiers atomically so API and Redis keys remain numeric.
        if (entity instanceof Movie movie && movie.getId() == null) {
            movie.setId(nextId("movies"));
        } else if (entity instanceof Auditorium auditorium && auditorium.getId() == null) {
            auditorium.setId(nextId("auditoriums"));
        } else if (entity instanceof Seat seat && seat.getId() == null) {
            seat.setId(nextId("seats"));
        } else if (entity instanceof Screening screening && screening.getId() == null) {
            screening.setId(nextId("screenings"));
        }
        return entity;
    }

    private long nextId(String name) {
        Document sequence = databaseFactory.getMongoDatabase().getCollection("sequences")
                .findOneAndUpdate(Filters.eq("_id", name), Updates.inc("value", 1L),
                        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        if (sequence == null || !(sequence.get("value") instanceof Number number)) {
            throw new IllegalStateException("Не удалось получить ID для " + name);
        }
        return number.longValue();
    }

    @Override
    public void run(ApplicationArguments args) {
        var database = databaseFactory.getMongoDatabase();
        database.getCollection("movies").createIndex(Indexes.ascending("slug"), new IndexOptions().unique(true));
        database.getCollection("auditoriums").createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));
        database.getCollection("seats").createIndex(
                Indexes.ascending("auditorium", "rowNumber", "seatNumber"), new IndexOptions().unique(true));
        database.getCollection("screenings").createIndex(
                Indexes.ascending("auditorium", "startsAt"), new IndexOptions().unique(true));
        database.getCollection("screenings").createIndex(Indexes.ascending("movie", "status", "startsAt"));
    }
}
