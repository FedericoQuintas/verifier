package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import org.springframework.stereotype.Service;

@Service
public class RedisOrderPlacedPublisher implements OrderPlacedPublisher {
    @Override
    public void publish(Event event) {

    }
}
