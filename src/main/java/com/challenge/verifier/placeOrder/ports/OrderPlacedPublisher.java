package com.challenge.verifier.placeOrder.ports;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.stream.PublisherResult;

public interface OrderPlacedPublisher {
    PublisherResult publish(Event event);
}
