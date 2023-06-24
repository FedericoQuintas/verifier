package com.challenge.verifier.placeOrder.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPersistentModel implements Serializable {

    private Long id;
    private String side;
    private int quantity;
    private BigDecimal price;
    private long timestamp;

    @JsonIgnore
    public boolean isOnBuySide() {
        return Side.BUY.name().equals(side);
    }
}
