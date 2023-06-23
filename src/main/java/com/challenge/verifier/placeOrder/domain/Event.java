package com.challenge.verifier.placeOrder.domain;

public record Event(Order order, EventType eventType) {

    public static Event with(Order order, EventType eventType) {
        return new Event(order, eventType);
    }

    public EventPersistentModel asPersistentModel() {
        return new EventPersistentModel(order.id().value(), order.side().name(), order.quantity().value(), order.price().value(), eventType.name(), order.timestamp());
    }

    public boolean isOnBuySide() {
        return order.isOnBuySide();
    }
}
