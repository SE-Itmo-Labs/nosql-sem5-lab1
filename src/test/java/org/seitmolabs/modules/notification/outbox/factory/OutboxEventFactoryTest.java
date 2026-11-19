package org.seitmolabs.modules.notification.outbox.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;

class OutboxEventFactoryTest {
    @Test
    void orderEventsKeepTheCurrentUsername() {
        CinemaOrder order = CinemaOrder.builder()
                .id(10L)
                .userId(42L)
                .totalAmount(new BigDecimal("450.00"))
                .build();
        OutboxEventFactory factory = new OutboxEventFactory();

        assertThat(factory.orderConfirmed(order, List.of(3L), "client01").getUsername())
                .isEqualTo("client01");
        assertThat(factory.orderCancelled(order, "client01").getUsername())
                .isEqualTo("client01");
    }
}
