package com.challenge.verifier.reconcileOrderBook.handler;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.Side;
import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import com.challenge.verifier.reconcileOrderBook.domain.ReconciliationResult;
import com.challenge.verifier.reconcileOrderBook.domain.TradeLogsResult;
import com.challenge.verifier.reconcileOrderBook.ports.TradesLogReader;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReconcileOrderBookCommandHandlerTest {

    private OrdersPriorityQueue ordersPriorityQueue;
    private ReconcileOrderBookCommandHandler reconcileOrderBook;
    private TradesLogReader logReader;

    @Before
    public void before() {
        ordersPriorityQueue = mock(OrdersPriorityQueue.class);
        logReader = mock(TradesLogReader.class);
        reconcileOrderBook = new ReconcileOrderBookCommandHandler(ordersPriorityQueue, logReader);
    }

    @Test
    public void reconcileOrderBookWithoutTrades() {
        when(logReader.readAll()).thenReturn(TradeLogsResult.empty());
        Order sellOrder10005 = new TestOrderBuilder().withId(10005).withQuantity(20000).withPrice(105).withSide(Side.SELL).build();
        Order sellOrder10001 = new TestOrderBuilder().withId(10001).withQuantity(500).withPrice(100).withSide(Side.SELL).build();
        Order sellOrder10002 = new TestOrderBuilder().withId(10002).withQuantity(10000).withPrice(100).withSide(Side.SELL).build();
        Order sellOrder10004 = new TestOrderBuilder().withId(10004).withQuantity(100).withPrice(103).withSide(Side.SELL).build();
        when(ordersPriorityQueue.readFrom(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder10001))
                .thenReturn(ReadQueueResult.with(sellOrder10002))
                .thenReturn(ReadQueueResult.with(sellOrder10004))
                .thenReturn(ReadQueueResult.with(sellOrder10005))
                .thenReturn(ReadQueueResult.empty());

        Order buyOrder10000 = new TestOrderBuilder().withId(10000).withQuantity(25500).withPrice(98).withSide(Side.BUY).build();
        Order buyOrder10003 = new TestOrderBuilder().withId(10003).withQuantity(50000).withPrice(99).withSide(Side.BUY).build();

        when(ordersPriorityQueue.readFrom(Side.BUY)).thenReturn(ReadQueueResult.with(buyOrder10003))
                .thenReturn(ReadQueueResult.with(buyOrder10000))
                .thenReturn(ReadQueueResult.empty());

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("8ff13aad3e61429bfb5ce0857e846567", result.output());
    }

    @Test
    public void reconcileOrderBookWithTrades() {

        when(logReader.readAll()).thenReturn(TradeLogsResult.with(List.of("trade 10006,10001,100,500",
                "trade 10006,10002,100,10000",
                "trade 10006,10004,103,100",
                "trade 10006,10005,105,5400")));

        Order sellOrder10005 = new TestOrderBuilder().withId(10005).withQuantity(14600).withPrice(105).withSide(Side.SELL).build();
        when(ordersPriorityQueue.readFrom(Side.SELL)).thenReturn(ReadQueueResult.with(sellOrder10005))
                .thenReturn(ReadQueueResult.empty());

        Order buyOrder10000 = new TestOrderBuilder().withId(10000).withQuantity(25500).withPrice(98).withSide(Side.BUY).build();
        Order buyOrder10003 = new TestOrderBuilder().withId(10003).withQuantity(50000).withPrice(99).withSide(Side.BUY).build();
        when(ordersPriorityQueue.readFrom(Side.BUY)).thenReturn(ReadQueueResult.with(buyOrder10003))
                .thenReturn(ReadQueueResult.with(buyOrder10000))
                .thenReturn(ReadQueueResult.empty());

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("ce8e7e5ab26ab5a7db6b7d30759cf02e", result.output());
    }
}
