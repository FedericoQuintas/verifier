package com.challenge.verifier.placeOrder.domain;

import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;

@RedisHash("Order")
@Data
public class OrderPersistentModel implements Serializable {
    private Long id;
    private String side;
    private int quantity;
    private BigDecimal price;
    private long timestamp;

    public OrderPersistentModel(Long id, String side, int quantity, BigDecimal price, long timestamp) {
        this.id = id;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public boolean isOnBuySide() {
        return Side.BUY.name().equals(side);
    }
}
