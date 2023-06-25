package com.challenge.verifier.placeOrder.ports;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.domain.Side;
import com.challenge.verifier.placeOrder.domain.SnapshotResult;
import com.challenge.verifier.placeOrder.stream.Result;

public interface OrdersPriorityQueue {
    Result add(OrderPersistentModel order);

    SnapshotResult snapshot();

    ReadQueueResult readFrom(Side matchingSide);
}
