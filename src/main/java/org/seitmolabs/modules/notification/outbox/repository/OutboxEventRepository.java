package org.seitmolabs.modules.notification.outbox.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface OutboxEventRepository extends MongoRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByDeliveredAtIsNullOrderByLastAttemptAtAscCreatedAtAsc(Pageable pageable);

    @Query("{ '_id': ?0, 'deliveredAt': null }")
    @Update("{ '$set': { 'deliveredAt': ?1 }, '$unset': { 'lastError': '' } }")
    void markDelivered(UUID id, Instant deliveredAt);

    @Query("{ '_id': ?0, 'deliveredAt': null }")
    @Update("{ '$inc': { 'attempts': 1 }, '$set': { 'lastError': ?1, 'lastAttemptAt': ?2 } }")
    void recordFailure(UUID id, String error, Instant attemptedAt);
}
