package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.domain.EventType;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.helper.OrderTestHelper;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PlaceOrderCommandHandlerTest {

    public static final Instant NOW = Instant.now();
    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private OrderPlacedPublisher publisher;
    private OrderRepository orderRepository;

    @BeforeEach
    public void before() {
        publisher = mock(OrderPlacedPublisher.class);
        orderRepository = mock(OrderRepository.class);
        placeOrderCommandHandler = new PlaceOrderCommandHandler(publisher, orderRepository);
    }

    @Test
    public void publishesOrderPlacedEvent() {
        Order order = OrderTestHelper.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED);
        placeOrderCommandHandler.place(order);
        verify(publisher).publish(event);
    }

    @Test
    public void storesEvent() {
        Order order = OrderTestHelper.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED);
        placeOrderCommandHandler.place(order);
        verify(orderRepository).add(event);
    }
}
