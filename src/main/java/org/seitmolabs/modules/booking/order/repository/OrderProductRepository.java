package org.seitmolabs.modules.booking.order.repository;

import java.util.List;

import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderProductRepository extends MongoRepository<OrderProduct, Long> {

    List<OrderProduct> findAllByOrderId(Long orderId);
}
