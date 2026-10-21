package org.seitmolabs.modules.booking.order.repository;

import java.util.List;
import java.util.Optional;

import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CinemaOrderRepository extends JpaRepository<CinemaOrder, Long> {

    Optional<CinemaOrder> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    Optional<CinemaOrder> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select orders from CinemaOrder orders where orders.id = :id and orders.user.id = :userId")
    Optional<CinemaOrder> findForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    List<CinemaOrder> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
