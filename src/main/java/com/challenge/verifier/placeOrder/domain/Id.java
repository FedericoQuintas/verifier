package com.challenge.verifier.placeOrder.domain;


public record Id(Long value) {
    public static Id of(Long value) {
        return new Id(value);
    }
}
