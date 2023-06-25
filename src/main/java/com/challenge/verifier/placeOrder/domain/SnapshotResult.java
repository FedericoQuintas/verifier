package com.challenge.verifier.placeOrder.domain;

import java.util.List;

public class SnapshotResult {
    private final boolean succeeded;
    private List<OrderPersistentModel> buyQueue;
    private List<OrderPersistentModel> sellQueue;

    public SnapshotResult(List<OrderPersistentModel> buyQueue, List<OrderPersistentModel> sellQueue) {
        this.buyQueue = buyQueue;
        this.sellQueue = sellQueue;
        this.succeeded = true;
    }

    public SnapshotResult(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public static SnapshotResult with(List<OrderPersistentModel> buyQueue, List<OrderPersistentModel> sellQueue) {
        return new SnapshotResult(buyQueue, sellQueue);
    }

    public static SnapshotResult error() {
        return new SnapshotResult(false);
    }

    public List<OrderPersistentModel> buyQueue() {
        return buyQueue;
    }

    public List<OrderPersistentModel> sellQueue() {
        return sellQueue;
    }

    public boolean failed() {
        return !succeeded;
    }
}
