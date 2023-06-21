package com.challenge.verifier.placeOrder.helper;

import com.challenge.verifier.placeOrder.domain.Order;

import java.time.Instant;

public class OrderTestHelper {

    public static Order buildOrder(){
        return Order.buildFrom("10000,B,98,25500", Instant.now());
    }
}
