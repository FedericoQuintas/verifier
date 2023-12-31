package com.challenge.verifier.common.helper;

import com.challenge.verifier.common.domain.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestOrderBuilder {

    private long orderId = 10000l;
    private Side side = Side.BUY;
    private int quantity = 25500;
    private int price = 98;
    private Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    public static Order buildOrder() {
        return Order.buildFrom("10000,B,98,25500", Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public Order build() {
        return new Order(Id.of(orderId), side, Quantity.of(quantity), Price.of(price), timestamp);
    }

    public TestOrderBuilder withSide(Side side) {
        this.side = side;
        return this;
    }

    public TestOrderBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public TestOrderBuilder withPrice(int price) {
        this.price = price;
        return this;
    }

    public TestOrderBuilder withId(int orderId) {
        this.orderId = orderId;
        return this;
    }
}
