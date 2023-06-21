package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.placeOrder.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderCommandHandler {
    public void place(Order order) {
        System.out.println("Received " + order);
    }
}
