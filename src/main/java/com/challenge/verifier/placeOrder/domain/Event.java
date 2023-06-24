package com.challenge.verifier.placeOrder.domain;

import java.time.Instant;

public record Event(Order order, EventType eventType, Instant timestamp) {

    public static Event with(Order order, EventType eventType, Instant timestamp) {
        return new Event(order, eventType, timestamp);
    }

    public EventPersistentModel asPersistentModel() {
        return new EventPersistentModel(order.id().value(), order.side().name(), order.quantity().value(), order.price().value(), eventType.name(), timestamp);
    }

}
