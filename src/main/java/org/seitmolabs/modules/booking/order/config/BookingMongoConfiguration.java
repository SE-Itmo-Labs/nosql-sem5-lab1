package org.seitmolabs.modules.booking.order.config;

import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
@Order(0)
public class BookingMongoConfiguration implements BeforeConvertCallback<Object>, ApplicationRunner {

    private final MongoDatabaseFactory databaseFactory;

    public BookingMongoConfiguration(MongoDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Bean
    MongoTransactionManager transactionManager() {
        TransactionOptions options = TransactionOptions.builder()
                .readConcern(ReadConcern.SNAPSHOT)
                .writeConcern(WriteConcern.MAJORITY)
                .build();
        return new MongoTransactionManager(databaseFactory, options) {
            @Override
            protected void doCommit(MongoTransactionObject transaction) throws Exception {
                // An uncertain commit result can be retried on the same session.
                for (int attempt = 1; ; attempt++) {
                    try {
                        super.doCommit(transaction);
                        return;
                    } catch (MongoException exception) {
                        if (attempt >= 3 || !exception.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)) {
                            throw exception;
                        }
                    }
                }
            }
        };
    }

    @Bean
    TransactionTemplate bookingTransactionTemplate(MongoTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Override
    public Object onBeforeConvert(Object entity, String collection) {
        if (entity instanceof CinemaOrder order && order.getId() == null) {
            order.setId(nextId(collection));
        } else if (entity instanceof Ticket ticket && ticket.getId() == null) {
            ticket.setId(nextId(collection));
        } else if (entity instanceof OrderProduct item && item.getId() == null) {
            item.setId(nextId(collection));
        }
        return entity;
    }

    private long nextId(String collection) {
        // Allocate outside the transaction so concurrent orders do not contend on a transactional counter.
        Document sequence = databaseFactory.getMongoDatabase().getCollection("sequences")
                .findOneAndUpdate(Filters.eq("_id", collection), Updates.inc("value", 1L),
                        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        if (sequence == null || !(sequence.get("value") instanceof Number number)) {
            throw new IllegalStateException("Не удалось получить ID для " + collection);
        }
        return number.longValue();
    }

    @Override
    public void run(ApplicationArguments args) {
        var database = databaseFactory.getMongoDatabase();
        var orders = database.getCollection("orders");
        orders.createIndex(Indexes.ascending("userId", "idempotencyKey"),
                new IndexOptions().unique(true));
        orders.createIndex(Indexes.ascending("holdId"), new IndexOptions().unique(true));
        orders.createIndex(Indexes.compoundIndex(Indexes.ascending("userId"), Indexes.descending("createdAt")));

        var tickets = database.getCollection("tickets");
        tickets.createIndex(Indexes.ascending("screeningId", "seatId"),
                new IndexOptions().unique(true).partialFilterExpression(Filters.eq("status", TicketStatus.CONFIRMED.name())));
        tickets.createIndex(Indexes.ascending("orderId"));
        database.getCollection("order_products").createIndex(Indexes.ascending("orderId", "productId"),
                new IndexOptions().unique(true));
        database.getCollection("outbox_events").createIndex(Indexes.ascending("deliveredAt", "lastAttemptAt", "createdAt"));
    }
}
