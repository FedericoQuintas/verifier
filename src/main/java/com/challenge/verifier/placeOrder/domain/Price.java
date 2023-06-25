package com.challenge.verifier.placeOrder.domain;

public record Price(Integer value) {
    public static Price of(Integer value) {
        return new Price(value);
    }

    public boolean isEqualOrGreaterThan(Price price) {
        return value.compareTo(price.value) >= 0;
    }

}
