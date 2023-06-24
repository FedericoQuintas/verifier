package com.challenge.verifier.placeOrder.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity(name = "Order_Events")
@Data
public class EventPersistentModel {

    protected EventPersistentModel() {
    }

    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "price", nullable = false)
    private BigDecimal price;
    @Column(name = "side", nullable = false)
    private String side;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "event_type", nullable = false)
    private String event_type;
    @Column(name = "event_time", nullable = false)
    private Instant event_time;

    public EventPersistentModel(Long id, String side, Integer quantity, BigDecimal price, String event_type, Instant event_time) {
        this.id = id;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.event_type = event_type;
        this.event_time = event_time;
    }

}
