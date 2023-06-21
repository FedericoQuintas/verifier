package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.domain.EventType;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderCommandHandler {

    private OrderPlacedPublisher publisher;
    private OrderRepository orderRepository;

    public PlaceOrderCommandHandler(OrderPlacedPublisher publisher, OrderRepository orderRepository){
        this.publisher = publisher;
        this.orderRepository = orderRepository;
    }

    public void place(Order order) {
        Event event = Event.with(order, EventType.ORDER_PLACED);
        publisher.publish(event);
        orderRepository.add(event);
    }
}
