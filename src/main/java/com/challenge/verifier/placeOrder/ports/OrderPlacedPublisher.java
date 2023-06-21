package com.challenge.verifier.placeOrder.ports;

import com.challenge.verifier.placeOrder.domain.Event;

public interface OrderPlacedPublisher {
    void publish(Event event);
}
