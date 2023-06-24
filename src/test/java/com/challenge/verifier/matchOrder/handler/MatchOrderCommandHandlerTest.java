package com.challenge.verifier.matchOrder.handler;

import com.challenge.verifier.matchOrder.MatchOrderCommandHandler;
import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.domain.EventType;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.Side;
import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class MatchOrderCommandHandlerTest {

    private OrdersPriorityQueue ordersPriorityQueue;
    private MatchOrderCommandHandler matchOrderCommandHandler;
    private OrderRepository orderRepository;

    @BeforeEach
    public void before() {
        ordersPriorityQueue = mock(OrdersPriorityQueue.class);
        orderRepository = mock(OrderRepository.class);
        matchOrderCommandHandler = new MatchOrderCommandHandler(ordersPriorityQueue, orderRepository);
    }

    @Test
    public void whenOtherSideQueueIsEmptyAddsOrderAsItIs() {
        Order order = TestOrderBuilder.buildOrder();
        when(ordersPriorityQueue.read(Side.SELL)).thenReturn(ReadQueueResult.empty());

        matchOrderCommandHandler.match(order);

        verify(ordersPriorityQueue).add(order.asPersistentModel());
    }

    @Test
    public void whenOtherSideQueueHasOneExactlyMatchingOfferThenAddsEventsAndDoesNotAddToPriorityQueue() {
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).build();
        Order sellOrder = new TestOrderBuilder().withSide(Side.SELL).build();
        when(ordersPriorityQueue.read(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder));

        matchOrderCommandHandler.match(buyOrder);

        verify(ordersPriorityQueue, never()).add(any());
        verify(orderRepository).saveAndFlush(Event.with(buyOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_FILLED).asPersistentModel());
        verify(orderRepository).saveAndFlush(Event.with(sellOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_FILLED).asPersistentModel());

    }

    @Test
    public void whenBuyOrderMatchesPriceAndItsPartiallyFilledAddsEventsAndAddsToPriorityQueue() {
        Order buyOrder = new TestOrderBuilder().withSide(Side.BUY).withQuantity(50).build();
        Order sellOrder = new TestOrderBuilder().withSide(Side.SELL).withQuantity(20).build();
        when(ordersPriorityQueue.read(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder));

        matchOrderCommandHandler.match(buyOrder);
        verify(orderRepository).saveAndFlush(Event.with(sellOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_FILLED).asPersistentModel());
        verify(orderRepository).saveAndFlush(Event.with(buyOrder.reduceQuantity(sellOrder.quantity()), EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
        verify(ordersPriorityQueue).add(buyOrder.reduceQuantity(sellOrder.quantity()).asPersistentModel());
    }
}
