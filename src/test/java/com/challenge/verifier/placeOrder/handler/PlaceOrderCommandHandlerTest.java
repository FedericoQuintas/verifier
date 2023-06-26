package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.common.domain.Event;
import com.challenge.verifier.common.domain.EventType;
import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.time.TimeProvider;
import com.challenge.verifier.common.domain.Result;
import com.challenge.verifier.common.helper.TestOrderBuilder;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.queryOrder.handler.StoreOrderEventCommandHandler;
import com.challenge.verifier.queryOrder.query.OrderEventQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

public class PlaceOrderCommandHandlerTest {

    public static final Instant NOW = Instant.now();
    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private OrderPlacedPublisher publisher;
    private TimeProvider timeProvider;
    private StoreOrderEventCommandHandler storeOrderEventCommandHandler;
    private OrderEventQueryService orderEventQueryService;

    @BeforeEach
    public void before() {
        publisher = mock(OrderPlacedPublisher.class);
        storeOrderEventCommandHandler = mock(StoreOrderEventCommandHandler.class);
        orderEventQueryService = mock(OrderEventQueryService.class);
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.now()).thenReturn(NOW);
        placeOrderCommandHandler = new PlaceOrderCommandHandler(publisher, storeOrderEventCommandHandler, orderEventQueryService, timeProvider);
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
        verify(storeOrderEventCommandHandler).store(event);
    }

    @Test
    public void discardsRepeatedOrder() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED, timeProvider.now());
        when(orderEventQueryService.existsById(event.asPersistentModel().getId())).thenReturn(true);
        placeOrderCommandHandler.place(order);
        verify(storeOrderEventCommandHandler, never()).store(event);
    }

    @Test
    public void itDoesNotStoreWhenPublishingFails() {
        Order order = TestOrderBuilder.buildOrder();
        Event event = Event.with(order, EventType.ORDER_PLACED, timeProvider.now());
        when(publisher.publish(any())).thenReturn(Result.error());
        placeOrderCommandHandler.place(order);
        verify(storeOrderEventCommandHandler, never()).store(event);
    }
}
