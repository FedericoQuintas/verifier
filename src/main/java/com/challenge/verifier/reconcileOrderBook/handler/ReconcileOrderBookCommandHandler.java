package com.challenge.verifier.reconcileOrderBook.handler;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.Side;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import com.challenge.verifier.reconcileOrderBook.domain.ReconciliationResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Locale;

@Service
public class ReconcileOrderBookCommandHandler {

    public static final int LEFT_PAD_EMPTY_SLOT = 18;
    public static final int LEFT_PAD_QUANTITY = 11;
    public static final int LEFT_PAD_PRICE = 6;
    private OrdersPriorityQueue ordersPriorityQueue;

    public ReconcileOrderBookCommandHandler(OrdersPriorityQueue ordersPriorityQueue) {
        this.ordersPriorityQueue = ordersPriorityQueue;
    }

    public ReconciliationResult reconcile() {
        String string = "";

        string = readPendingOrders(string);
        try {
            return ReconciliationResult.withOutput(hash(string));
        } catch (NoSuchAlgorithmException e) {
            return ReconciliationResult.withError(e.getMessage());
        }
    }

    private String readPendingOrders(String string) {
        ReadQueueResult readBuyQueueResult = ordersPriorityQueue.readFrom(Side.BUY);
        ReadQueueResult readSellQueueResult = ordersPriorityQueue.readFrom(Side.SELL);
        while (!readBuyQueueResult.isEmpty() || !readSellQueueResult.isEmpty()) {
            string = append(string, readBuyQueueResult, readSellQueueResult);
            readBuyQueueResult = ordersPriorityQueue.readFrom(Side.BUY);
            readSellQueueResult = ordersPriorityQueue.readFrom(Side.SELL);
            string += "\n";
        }
        return string;
    }

    private String hash(String string) throws NoSuchAlgorithmException {
        return DigestUtils.md5Hex(string);
    }

    private String append(String currentString, ReadQueueResult readBuyQueueResult, ReadQueueResult readSellQueueResult) {
        currentString = fillBuySide(currentString, readBuyQueueResult);
        currentString += " | ";
        currentString = fillSellSide(currentString, readSellQueueResult);
        return currentString;
    }

    private static String fillSellSide(String currentString, ReadQueueResult readSellQueueResult) {
        if (!readSellQueueResult.isEmpty()) {
            Order order = readSellQueueResult.order();
            currentString += padLeftZeros(String.valueOf(order.price().value()), LEFT_PAD_PRICE) + " " + padLeftZeros(formatForThousands(order), LEFT_PAD_QUANTITY);
        } else {
            currentString = fillEmptySlot(currentString); // Note: I don't have a test to verify if this is actually expected for the Sell side too.
        }
        return currentString;
    }

    private static String fillBuySide(String currentString, ReadQueueResult readBuyQueueResult) {
        if (!readBuyQueueResult.isEmpty()) {
            Order order = readBuyQueueResult.order();
            currentString += padLeftZeros(formatForThousands(order), LEFT_PAD_QUANTITY) + " " + padLeftZeros(String.valueOf(order.price().value()), LEFT_PAD_PRICE);
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
