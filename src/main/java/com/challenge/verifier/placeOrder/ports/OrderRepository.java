package com.challenge.verifier.placeOrder.ports;

import com.challenge.verifier.placeOrder.domain.Event;

public interface OrderRepository {
    void add(Event event);
}
