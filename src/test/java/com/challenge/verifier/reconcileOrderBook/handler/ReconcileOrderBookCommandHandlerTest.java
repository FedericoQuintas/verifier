package com.challenge.verifier.reconcileOrderBook.handler;

import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.domain.Side;
import com.challenge.verifier.placeOrder.domain.SnapshotResult;
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
        OrderPersistentModel sellOrder10005 = new TestOrderBuilder().withId(10005).withQuantity(20000).withPrice(105).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel sellOrder10001 = new TestOrderBuilder().withId(10001).withQuantity(500).withPrice(100).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel sellOrder10002 = new TestOrderBuilder().withId(10002).withQuantity(10000).withPrice(100).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel sellOrder10004 = new TestOrderBuilder().withId(10004).withQuantity(100).withPrice(103).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel buyOrder10000 = new TestOrderBuilder().withId(10000).withQuantity(25500).withPrice(98).withSide(Side.BUY).build().asPersistentModel();
        OrderPersistentModel buyOrder10003 = new TestOrderBuilder().withId(10003).withQuantity(50000).withPrice(99).withSide(Side.BUY).build().asPersistentModel();
        when(ordersPriorityQueue.snapshot()).thenReturn(SnapshotResult.with(List.of(buyOrder10000, buyOrder10003), List.of(sellOrder10001,
                sellOrder10002,
                sellOrder10004,
                sellOrder10005)));

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("8ff13aad3e61429bfb5ce0857e846567", result.output());
    }

    @Test
    public void reconcileOrderBookWithoutOrders() {
        when(logReader.readAll()).thenReturn(TradeLogsResult.empty());
        when(ordersPriorityQueue.snapshot()).thenReturn(SnapshotResult.with(List.of(), List.of()));

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result.output());
    }

    @Test
    public void reconcileOrderBookWithoutSellOrders() {
        when(logReader.readAll()).thenReturn(TradeLogsResult.empty());
        OrderPersistentModel buyOrder10000 = new TestOrderBuilder().withId(10000).withQuantity(25500).withPrice(98).withSide(Side.BUY).build().asPersistentModel();
        OrderPersistentModel buyOrder10003 = new TestOrderBuilder().withId(10003).withQuantity(50000).withPrice(99).withSide(Side.BUY).build().asPersistentModel();
        when(ordersPriorityQueue.snapshot()).thenReturn(SnapshotResult.with(List.of(buyOrder10000, buyOrder10003), List.of()));

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("76f91585ca2d3aa9eab2dd2c5f4c5ced", result.output());
    }

    @Test
    public void reconcileOrderBookWithoutBuyOrders() {
        when(logReader.readAll()).thenReturn(TradeLogsResult.empty());
        OrderPersistentModel sellOrder10005 = new TestOrderBuilder().withId(10005).withQuantity(20000).withPrice(105).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel sellOrder10001 = new TestOrderBuilder().withId(10001).withQuantity(500).withPrice(100).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel sellOrder10002 = new TestOrderBuilder().withId(10002).withQuantity(10000).withPrice(100).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel sellOrder10004 = new TestOrderBuilder().withId(10004).withQuantity(100).withPrice(103).withSide(Side.SELL).build().asPersistentModel();
        when(ordersPriorityQueue.snapshot()).thenReturn(SnapshotResult.with(List.of(), List.of(sellOrder10001,
                sellOrder10002,
                sellOrder10004,
                sellOrder10005)));

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("061e37b1718d26b0a7c656ada5745d01", result.output());
    }

    @Test
    public void reconcileOrderBookWithTrades() {

        when(logReader.readAll()).thenReturn(TradeLogsResult.with(List.of("trade 10006,10001,100,500",
                "trade 10006,10002,100,10000",
                "trade 10006,10004,103,100",
                "trade 10006,10005,105,5400")));

        OrderPersistentModel sellOrder10005 = new TestOrderBuilder().withId(10005).withQuantity(14600).withPrice(105).withSide(Side.SELL).build().asPersistentModel();
        OrderPersistentModel buyOrder10000 = new TestOrderBuilder().withId(10000).withQuantity(25500).withPrice(98).withSide(Side.BUY).build().asPersistentModel();
        OrderPersistentModel buyOrder10003 = new TestOrderBuilder().withId(10003).withQuantity(50000).withPrice(99).withSide(Side.BUY).build().asPersistentModel();
        when(ordersPriorityQueue.snapshot()).thenReturn(SnapshotResult.with(List.of(buyOrder10000, buyOrder10003), List.of(sellOrder10005)));

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("ce8e7e5ab26ab5a7db6b7d30759cf02e", result.output());
    }

    @Test
    public void reconcileOrderBookOnlyWithTrades() {
        when(logReader.readAll()).thenReturn(TradeLogsResult.with(List.of("trade 10006,10001,100,500",
                "trade 10006,10002,100,10000",
                "trade 10006,10004,103,100",
                "trade 10006,10005,105,5400")));
        when(ordersPriorityQueue.snapshot()).thenReturn(SnapshotResult.with(List.of(), List.of()));

        ReconciliationResult result = reconcileOrderBook.reconcile();
        assertEquals("f8ab9bf8688c0edaa0730b4b28a6462b", result.output());
    }
}
