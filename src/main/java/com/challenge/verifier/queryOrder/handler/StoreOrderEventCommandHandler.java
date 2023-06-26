package com.challenge.verifier.queryOrder.handler;

import com.challenge.verifier.common.domain.Event;
import com.challenge.verifier.queryOrder.ports.OrderEventRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreOrderEventCommandHandler {

    private OrderEventRepository orderEventRepository;

    public StoreOrderEventCommandHandler(OrderEventRepository orderEventRepository) {
        this.orderEventRepository = orderEventRepository;
    }

    public void store(Event event) {
        orderEventRepository.save(event.asPersistentModel());
    }
}
