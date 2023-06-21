package com.challenge.verifier.placeOrder.domain;

import java.math.BigDecimal;

public record Price(BigDecimal value) {
    public static Price of(BigDecimal value) {
        return new Price(value);
    }
}
