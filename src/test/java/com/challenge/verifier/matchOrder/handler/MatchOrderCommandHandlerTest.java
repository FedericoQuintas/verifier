package com.challenge.verifier.matchOrder.handler;

import com.challenge.verifier.matchOrder.MatchOrderCommandHandler;
import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.*;
import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class MatchOrderCommandHandlerTest {

    public static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private OrdersPriorityQueue ordersPriorityQueue;
    private MatchOrderCommandHandler matchOrderCommandHandler;
    private OrderRepository orderRepository;
    private TimeProvider timeProvider;

    @BeforeEach
    public void before() {
        ordersPriorityQueue = mock(OrdersPriorityQueue.class);
        orderRepository = mock(OrderRepository.class);
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.now()).thenReturn(NOW);
        matchOrderCommandHandler = new MatchOrderCommandHandler(ordersPriorityQueue, orderRepository, timeProvider);
    }

    @Test
    public void ifOrderAlreadyFilledThenDoesNothing() {
        Order order = new TestOrderBuilder().build();
        Event event = Event.with(order, EventType.ORDER_FILLED, Instant.now());
        when(orderRepository.findAllById(List.of(order.id().value()))).thenReturn(List.of(event.asPersistentModel()));

        matchOrderCommandHandler.match(order);

        verify(ordersPriorityQueue, never()).readFrom(any());
    }

    @Test
    public void whenOtherSideQueueIsEmptyAddsOrderAsItIs() {
        Order order = TestOrderBuilder.buildOrder();
        when(ordersPriorityQueue.readFrom(Side.SELL)).thenReturn(ReadQueueResult.empty());

        matchOrderCommandHandler.match(order);

        verify(ordersPriorityQueue).add(order.asPersistentModel());
    }

    @Test
    public void whenOtherSideQueueHasOneExactlyMatchingOfferThenAddsEventsAndDoesNotAddToPriorityQueue() {
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).build();
        Order sellOrder = new TestOrderBuilder().withSide(Side.SELL).build();
        when(ordersPriorityQueue.readFrom(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder));

        matchOrderCommandHandler.match(buyOrder);

        verify(ordersPriorityQueue, never()).add(any());
        verify(orderRepository).saveAndFlush(Event.with(buyOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());
        verify(orderRepository).saveAndFlush(Event.with(sellOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());
    }

    @Test
    public void whenBuyOrderMatchesPriceAndItsPartiallyFilledAddsEventsAndAddsToPriorityQueue() {
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).withQuantity(50).build();
        Order sellOrder = new TestOrderBuilder().withSide(Side.SELL).withQuantity(20).build();
        when(ordersPriorityQueue.readFrom(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder)).thenReturn(ReadQueueResult.empty());

        matchOrderCommandHandler.match(buyOrder);
        verify(orderRepository).saveAndFlush(Event.with(sellOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());
        verify(orderRepository).saveAndFlush(Event.with(buyOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_PARTIALLY_FILLED, NOW).asPersistentModel());
        verify(ordersPriorityQueue).add(buyOrder.reduceQuantity(sellOrder.quantity()).asPersistentModel());
        verify(ordersPriorityQueue, never()).add(sellOrder.reduceQuantity(sellOrder.quantity()).asPersistentModel());
    }

    @Test
    public void whenBuyOrderMatchesPriceAndItsFullyFilledAddsEventsAndAddsTheOtherOfferToPriorityQueue() {
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).withQuantity(20).build();
        Order sellOrder = new TestOrderBuilder().withSide(Side.SELL).withQuantity(50).build();
        when(ordersPriorityQueue.readFrom(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder));

        matchOrderCommandHandler.match(buyOrder);
        verify(orderRepository).saveAndFlush(Event.with(sellOrder.reduceQuantity(buyOrder.quantity()), EventType.ORDER_PARTIALLY_FILLED, NOW).asPersistentModel());
        verify(orderRepository).saveAndFlush(Event.with(buyOrder.reduceQuantity(buyOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());
        verify(ordersPriorityQueue).add(sellOrder.reduceQuantity(buyOrder.quantity()).asPersistentModel());
        verify(ordersPriorityQueue, never()).add(buyOrder.reduceQuantity(buyOrder.quantity()).asPersistentModel());
    }

    @Test
    public void whenSellOrderIsHigherThanHighestBuyOrderThenOnlyAddsToPriorityQueue() {
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).withPrice(50).build();
        Order sellOrder = new TestOrderBuilder().withSide(Side.SELL).withPrice(100).build();
        when(ordersPriorityQueue.readFrom(Side.BUY)).thenReturn(ReadQueueResult.with(buyOrder));

        matchOrderCommandHandler.match(sellOrder);

        verify(orderRepository, never()).saveAndFlush(any());
        verify(ordersPriorityQueue).add(sellOrder.asPersistentModel());
        verify(ordersPriorityQueue).add(buyOrder.asPersistentModel());
    }

    @Test
    public void whenBuyOrderIsLowerThanHighestSellOrderThenOnlyAddsToPriorityQueue() {
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).withPrice(50).build();
        Order sellOrder = new TestOrderBuilder().withSide(Side.SELL).withPrice(100).build();
        when(ordersPriorityQueue.readFrom(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder));

        matchOrderCommandHandler.match(buyOrder);

        verify(orderRepository, never()).saveAndFlush(any());
        verify(ordersPriorityQueue).add(sellOrder.asPersistentModel());
        verify(ordersPriorityQueue).add(buyOrder.asPersistentModel());
    }

    @Test
    public void whenBuyOrderMatchesPriceAndItsPartiallyFilledTwiceThenAddsTwoEventsAndAddsToPriorityQueue() {
        ArgumentCaptor<EventPersistentModel> captor = ArgumentCaptor.forClass(EventPersistentModel.class);
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).withQuantity(50).build();
        Order firstSellOrder = new TestOrderBuilder().withSide(Side.SELL).withQuantity(20).build();
        Order secondSellOrder = new TestOrderBuilder().withSide(Side.SELL).withQuantity(25).build();
        when(ordersPriorityQueue.readFrom(Side.SELL))
                .thenReturn(ReadQueueResult.with(firstSellOrder))
                .thenReturn(ReadQueueResult.with(secondSellOrder))
                .thenReturn(ReadQueueResult.empty());

        matchOrderCommandHandler.match(buyOrder);
        verify(orderRepository, times(4)).saveAndFlush(captor.capture());

        List<EventPersistentModel> allValues = captor.getAllValues();
        assertEquals(allValues.get(0), Event.with(buyOrder.reduceQuantity(firstSellOrder.quantity()), EventType.ORDER_PARTIALLY_FILLED, NOW).asPersistentModel());
        assertEquals(allValues.get(1), Event.with(firstSellOrder.reduceQuantity(firstSellOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());
        assertEquals(allValues.get(2), Event.with(buyOrder.reduceQuantity(firstSellOrder.quantity()).reduceQuantity(secondSellOrder.quantity()), EventType.ORDER_PARTIALLY_FILLED, NOW).asPersistentModel());
        assertEquals(allValues.get(3), Event.with(secondSellOrder.reduceQuantity(secondSellOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());

        verify(ordersPriorityQueue).add(buyOrder.reduceQuantity(firstSellOrder.quantity()).reduceQuantity(secondSellOrder.quantity()).asPersistentModel());
        verify(ordersPriorityQueue, never()).add(firstSellOrder.reduceQuantity(firstSellOrder.quantity()).asPersistentModel());
        verify(ordersPriorityQueue, never()).add(secondSellOrder.reduceQuantity(secondSellOrder.quantity()).asPersistentModel());
    }

    @Test
    public void whenBuyOrderMatchesPriceAndItsFullyFilledThenAddsTwoEventsAndAddsRemainingMatchingOrderToPriorityQueue() {
        ArgumentCaptor<EventPersistentModel> captor = ArgumentCaptor.forClass(EventPersistentModel.class);
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).withQuantity(50).build();
        Order firstSellOrder = new TestOrderBuilder().withSide(Side.SELL).withQuantity(20).build();
        Order secondSellOrder = new TestOrderBuilder().withSide(Side.SELL).withQuantity(35).build();

        when(ordersPriorityQueue.readFrom(Side.SELL))
                .thenReturn(ReadQueueResult.with(firstSellOrder))
                .thenReturn(ReadQueueResult.with(secondSellOrder));

        matchOrderCommandHandler.match(buyOrder);
        verify(orderRepository, times(4)).saveAndFlush(captor.capture());
        List<EventPersistentModel> allValues = captor.getAllValues();
        assertEquals(allValues.get(0), Event.with(buyOrder.reduceQuantity(firstSellOrder.quantity()), EventType.ORDER_PARTIALLY_FILLED, NOW).asPersistentModel());
        assertEquals(allValues.get(1), Event.with(firstSellOrder.reduceQuantity(firstSellOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());
        assertEquals(allValues.get(2), Event.with(buyOrder.reduceQuantity(buyOrder.quantity()), EventType.ORDER_FILLED, NOW).asPersistentModel());
        assertEquals(allValues.get(3), Event.with(secondSellOrder.reduceQuantity(Quantity.of(30)), EventType.ORDER_PARTIALLY_FILLED, NOW).asPersistentModel());
    }

    @Test
    public void whenReadFromQueueFailsOnlyAddsToQueue() {
        Order order = new TestOrderBuilder().build();
        when(ordersPriorityQueue.readFrom(any())).thenReturn(ReadQueueResult.error());
        matchOrderCommandHandler.match(order);

        verify(ordersPriorityQueue).add(order.asPersistentModel());
    }
}
