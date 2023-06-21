package com.challenge.verifier.placeOrder.domain;


public record Id(String value) {
    public static Id of(String value) {
        return new Id(value);
    }
}
