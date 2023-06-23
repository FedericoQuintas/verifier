package com.challenge.verifier.placeOrder.ports;

import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.stream.Result;

public interface OrdersPriorityQueue {
    Result add(OrderPersistentModel order);
}
