package com.challenge.verifier.placeOrder.ports;

import com.challenge.verifier.common.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.domain.Result;

public interface OrderPlacedPublisher {
    Result publish(OrderPersistentModel order);
}
