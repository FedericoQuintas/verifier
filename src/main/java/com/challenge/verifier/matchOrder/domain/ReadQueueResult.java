package com.challenge.verifier.matchOrder.domain;

import com.challenge.verifier.placeOrder.domain.Order;

public class ReadQueueResult {

    private Order order;

    private ReadQueueResult(Order order) {
        this.order = order;
    }

    private ReadQueueResult() {
    }

    public static ReadQueueResult empty() {
        return new ReadQueueResult();
    }

    public static ReadQueueResult with(Order order) {
        return new ReadQueueResult(order);
    }

    public boolean isEmpty() {
        return order == null;
    }

    public Order order() {
        return order;
    }
}
