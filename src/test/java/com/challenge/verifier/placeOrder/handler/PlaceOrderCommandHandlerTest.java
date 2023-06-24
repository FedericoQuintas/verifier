package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.domain.EventType;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import com.challenge.verifier.placeOrder.stream.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class PlaceOrderCommandHandlerTest {

    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private OrderPlacedPublisher publisher;
    private OrderRepository orderRepository;

    @BeforeEach
    public void before() {
        publisher = mock(OrderPlacedPublisher.class);
        orderRepository = mock(OrderRepository.class);
        placeOrderCommandHandler = new PlaceOrderCommandHandler(publisher, orderRepository);
        when(publisher.publish(any())).thenReturn(Result.ok());
    }

    @Test
    public void publishesOrderPlacedEvent() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED);
        placeOrderCommandHandler.place(order);
        verify(publisher).publish(order.asPersistentModel());
    }

    @Test
    public void storesEvent() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED);
        placeOrderCommandHandler.place(order);
        verify(orderRepository).saveAndFlush(event.asPersistentModel());
    }

    @Test
    public void discardsRepeatedOrder() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED);
        when(orderRepository.existsById(event.asPersistentModel().getId())).thenReturn(true);
        placeOrderCommandHandler.place(order);
        verify(orderRepository, never()).saveAndFlush(event.asPersistentModel());
    }

    @Test
    public void itDoesNotStoreWhenPublishingFails() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED);
        when(publisher.publish(any())).thenReturn(Result.error());
        placeOrderCommandHandler.place(order);
        verify(orderRepository, never()).saveAndFlush(event.asPersistentModel());
    }
}
