package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.common.domain.Event;
import com.challenge.verifier.common.domain.EventType;
import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.time.TimeProvider;
import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import com.challenge.verifier.placeOrder.stream.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

public class PlaceOrderCommandHandlerTest {

    public static final Instant NOW = Instant.now();
    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private OrderPlacedPublisher publisher;
    private OrderRepository orderRepository;
    private TimeProvider timeProvider;

    @BeforeEach
    public void before() {
        publisher = mock(OrderPlacedPublisher.class);
        orderRepository = mock(OrderRepository.class);
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.now()).thenReturn(NOW);
        placeOrderCommandHandler = new PlaceOrderCommandHandler(publisher, orderRepository, timeProvider);
        when(publisher.publish(any())).thenReturn(Result.ok());
    }

    @Test
    public void publishesOrderPlacedEvent() {
        Order order = TestOrderBuilder.buildOrder();
        placeOrderCommandHandler.place(order);
        verify(publisher).publish(order.asPersistentModel());
    }

    @Test
    public void storesEvent() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED, timeProvider.now());
        placeOrderCommandHandler.place(order);
        verify(orderRepository).saveAndFlush(event.asPersistentModel());
    }

    @Test
    public void discardsRepeatedOrder() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED, timeProvider.now());
        when(orderRepository.existsById(event.asPersistentModel().getId())).thenReturn(true);
        placeOrderCommandHandler.place(order);
        verify(orderRepository, never()).saveAndFlush(event.asPersistentModel());
    }

    @Test
    public void itDoesNotStoreWhenPublishingFails() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED, timeProvider.now());
        when(publisher.publish(any())).thenReturn(Result.error());
        placeOrderCommandHandler.place(order);
        verify(orderRepository, never()).saveAndFlush(event.asPersistentModel());
    }
}
