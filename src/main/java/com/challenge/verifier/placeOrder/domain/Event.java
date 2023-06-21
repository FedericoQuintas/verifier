package com.challenge.verifier.placeOrder.domain;

public record Event(Order order, EventType orderPlaced) {

    public static Event with(Order order, EventType eventType) {
        return new Event(order, eventType);
    }
}
