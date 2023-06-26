package com.challenge.verifier.reconcileOrderBook.handler;

import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.domain.OrderPersistentModel;
import com.challenge.verifier.reconcileOrderBook.domain.SnapshotResult;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import com.challenge.verifier.reconcileOrderBook.domain.ReconciliationResult;
import com.challenge.verifier.reconcileOrderBook.domain.TradeLogsResult;
import com.challenge.verifier.reconcileOrderBook.ports.TradesLogReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class ReconcileOrderBookCommandHandler {

    public static final int LEFT_PAD_EMPTY_SLOT = 18;
    public static final int LEFT_PAD_QUANTITY = 11;
    public static final int LEFT_PAD_PRICE = 6;
    private OrdersPriorityQueue ordersPriorityQueue;
    private TradesLogReader tradesLogReader;

    public ReconcileOrderBookCommandHandler(OrdersPriorityQueue ordersPriorityQueue, TradesLogReader tradesLogReader) {
        this.ordersPriorityQueue = ordersPriorityQueue;
        this.tradesLogReader = tradesLogReader;
    }

    public ReconciliationResult reconcile() {
        String string = "";
        string = readFromTradesLog(string);
        string = readPendingOrders(string);
        try {
            return ReconciliationResult.withOutput(hash(string));
        } catch (NoSuchAlgorithmException e) {
            return ReconciliationResult.withError(e.getMessage());
        }
    }

    private String readFromTradesLog(String string) {
        TradeLogsResult tradeLogsResult = tradesLogReader.readAll();
        StringBuilder stringBuilder = new StringBuilder(string);
        for (String log : tradeLogsResult.logs) {
            stringBuilder.append(log);
            stringBuilder.append("\n");
        }
        string = stringBuilder.toString();
        return string;
    }

    private String readPendingOrders(String string) {
        SnapshotResult snapshot = ordersPriorityQueue.snapshot();
        if (snapshot.failed()) throw new RuntimeException("Reconciliation failed");
        List<OrderPersistentModel> buyQueue = getAndReverseBuyQueue(snapshot.buyQueue());
        List<OrderPersistentModel> sellQueue = snapshot.sellQueue();
        Iterator<OrderPersistentModel> buyQueueIterator = buyQueue.iterator();
        Iterator<OrderPersistentModel> sellQueueIterator = sellQueue.iterator();
        while (buyQueueIterator.hasNext() || sellQueueIterator.hasNext()) {
            OrderPersistentModel nextBuy = buyQueueIterator.hasNext() ? buyQueueIterator.next() : null;
            OrderPersistentModel nextSell = sellQueueIterator.hasNext() ? sellQueueIterator.next() : null;
            string = append(string, nextBuy, nextSell);
            string += "\n";
        }
        return string;
    }

    private List<OrderPersistentModel> getAndReverseBuyQueue(List<OrderPersistentModel> queue) {
        List<OrderPersistentModel> buyQueue = new ArrayList<>(queue);
        Collections.reverse(buyQueue);
        return buyQueue;
    }

    private String hash(String string) throws NoSuchAlgorithmException {
        return DigestUtils.md5Hex(string);
    }

    private String append(String currentString, OrderPersistentModel buyOrder, OrderPersistentModel sellOrder) {
        currentString = fillSide(currentString, buyOrder);
        currentString += " | ";
        currentString = fillSide(currentString, sellOrder);
        return currentString;
    }

    private static String fillSide(String currentString, OrderPersistentModel orderPersistentModel) {
        if (orderPersistentModel != null) {
            Order order = Order.buildFrom(orderPersistentModel);
            currentString += order.isOnBuySide() ? padLeftZeros(formatForThousands(order), LEFT_PAD_QUANTITY)
                    + " " + padLeftZeros(String.valueOf(order.price().value()), LEFT_PAD_PRICE)
                    : padLeftZeros(String.valueOf(order.price().value()), LEFT_PAD_PRICE)
                    + " " + padLeftZeros(formatForThousands(order), LEFT_PAD_QUANTITY);
        } else {
            currentString = fillEmptySlot(currentString);
        }
        return currentString;
    }

    private static String formatForThousands(Order order) {
        return String.format(Locale.US, "%,d", order.quantity().value());
    }

    private static String fillEmptySlot(String currentString) {
        currentString += " ".repeat(LEFT_PAD_EMPTY_SLOT);
        return currentString;
    }

    private static String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append(" ");
        }
        sb.append(inputString);

        return sb.toString();
    }
}
