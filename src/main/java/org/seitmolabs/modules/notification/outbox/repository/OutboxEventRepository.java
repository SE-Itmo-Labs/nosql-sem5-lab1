package org.seitmolabs.modules.notification.outbox.repository;

import java.util.List;
import java.util.UUID;

import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByDeliveredAtIsNullOrderByCreatedAt(Pageable pageable);
}
