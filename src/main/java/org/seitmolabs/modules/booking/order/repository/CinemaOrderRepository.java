package org.seitmolabs.modules.booking.order.repository;

import java.util.List;
import java.util.Optional;

import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaOrderRepository extends JpaRepository<CinemaOrder, Long> {

    Optional<CinemaOrder> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    Optional<CinemaOrder> findByIdAndUserId(Long id, Long userId);

    List<CinemaOrder> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
