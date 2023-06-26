package com.challenge.verifier.common.domain;

public record Quantity(int value) {
    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public Quantity minus(Quantity quantity) {
        return Quantity.of(value - quantity.value);
    }

    public boolean isMoreThan(Quantity quantity) {
        return value > quantity.value;
    }
}
