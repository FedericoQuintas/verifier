package com.challenge.verifier.placeOrder.domain;

import java.time.Instant;
import java.util.List;

public record Event(Order order, EventType eventType, Instant timestamp) {

    public static Event with(Order order, EventType eventType, Instant timestamp) {
        return new Event(order, eventType, timestamp);
    }

    public static boolean isOrderFilled(List<EventPersistentModel> orderEvents) {
        return orderEvents.stream().anyMatch(EventPersistentModel::isOrderFilled);
    }

    public EventPersistentModel asPersistentModel() {
        return new EventPersistentModel(order.id().value(), order.side().name(), order.quantity().value(), order.price().value(), eventType.name(), timestamp);
    }

}
