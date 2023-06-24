package com.challenge.verifier.matchOrder.domain;

import com.challenge.verifier.placeOrder.domain.Order;

public class ReadQueueResult {

    private Order order;
    private boolean succeeded;

    private ReadQueueResult(Order order) {
        this.order = order;
        this.succeeded = true;
    }

    private ReadQueueResult() {
        this.succeeded = true;
    }

    public ReadQueueResult(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public static ReadQueueResult empty() {
        return new ReadQueueResult();
    }

    public static ReadQueueResult with(Order order) {
        return new ReadQueueResult(order);
    }


    public static ReadQueueResult error() {
        return new ReadQueueResult(false);
    }

    public boolean isEmpty() {
        return order == null;
    }

    public Order order() {
        return order;
    }

    public boolean succeeded() {
        return succeeded;
    }

}
