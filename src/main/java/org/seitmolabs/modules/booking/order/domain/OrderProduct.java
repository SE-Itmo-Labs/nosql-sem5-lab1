package org.seitmolabs.modules.booking.order.domain;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "order_products")
public class OrderProduct {

    @Id
    private Long id;

    private Long orderId;

    private Long productId;

    private int quantity;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal unitPriceSnapshot;
}
