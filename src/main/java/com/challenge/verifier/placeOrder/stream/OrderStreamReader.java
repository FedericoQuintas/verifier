package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.placeOrder.handler.PlaceOrderCommandHandler;
import org.springframework.stereotype.Service;

import java.util.Scanner;
import com.challenge.verifier.placeOrder.domain.Order;

@Service
public class OrderStreamReader {

    private PlaceOrderCommandHandler placeOrderCommandHandler;

    public OrderStreamReader(PlaceOrderCommandHandler placeOrderCommandHandler){
        this.placeOrderCommandHandler = placeOrderCommandHandler;
    }

    public void read() {
        Scanner scanner = new Scanner(System.in);
        String nextLine = scanner.nextLine();
        placeOrderCommandHandler.place(Order.buildFrom(nextLine));
    }
}
