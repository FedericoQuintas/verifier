package com.challenge.verifier.matchOrder.ports;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.common.domain.OrderPersistentModel;
import com.challenge.verifier.common.domain.Side;
import com.challenge.verifier.matchOrder.domain.SnapshotResult;
import com.challenge.verifier.matchOrder.domain.Result;

public interface OrdersPriorityQueue {
    Result add(OrderPersistentModel order);

    SnapshotResult snapshot();

    ReadQueueResult readFrom(Side matchingSide);

    void deleteAll();
}
