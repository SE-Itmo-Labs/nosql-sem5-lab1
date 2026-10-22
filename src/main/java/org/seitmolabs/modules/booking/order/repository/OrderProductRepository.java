package org.seitmolabs.modules.booking.order.repository;

import java.util.List;

import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query("""
            select item from OrderProduct item
            join fetch item.product product
            where item.order.id = :orderId
            order by product.name
            """)
    List<OrderProduct> findAllForOrder(@Param("orderId") Long orderId);
}
