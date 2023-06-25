package com.challenge.verifier.placeOrder.domain;

import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class OrderTest {

    public static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Test
    public void buildBuyOrderFromString() {
        Order order = TestOrderBuilder.buildOrder();
        assertEquals(order.id(), Id.of(10000L));
        assertEquals(order.side(), Side.BUY);
        assertEquals(order.price(), Price.of(98));
        assertEquals(order.quantity(), Quantity.of(25500));
    }

    @Test
    public void buildSellOrderFromString() {
        Order order = Order.buildFrom("10000,S,98,25500", NOW);
        assertEquals(order.side(), Side.SELL);
    }

    @Test
    public void convertsToPersistentModel() {
        Order order = Order.buildFrom("10000,S,98,25500", NOW);
        OrderPersistentModel orderPersistentModel = order.asPersistentModel();
        assertEquals(orderPersistentModel.getPrice(), order.price().value());
        assertEquals(orderPersistentModel.getTimestamp(), order.timestamp().toEpochMilli());
        assertEquals(orderPersistentModel.getSide(), order.side().name());
        assertEquals(orderPersistentModel.getId(), order.id().value());
        assertEquals(orderPersistentModel.getQuantity(), order.quantity().value());
    }

    @Test
    public void convertFromPersistentModel() {
        Order order = TestOrderBuilder.buildOrder();
        assertEquals(order, Order.buildFrom(order.asPersistentModel()));
    }

    @Test
    public void returnsIsOnBuySide() {
        assertTrue(new TestOrderBuilder().withSide(Side.BUY).build().isOnBuySide());
        assertFalse(new TestOrderBuilder().withSide(Side.SELL).build().isOnBuySide());
    }

    @Test
    public void hasRemainingQuantity() {
        assertTrue(new TestOrderBuilder().withQuantity(100).build().reduceQuantity(Quantity.of(30)).hasRemainingQuantity());
    }

    @Test
    public void reducesQuantity() {
        Order order = new TestOrderBuilder().withQuantity(100).build().reduceQuantity(Quantity.of(30));
        assertEquals(Quantity.of(70), order.quantity());
    }

    @Test
    public void throwsExceptionWhenSideIsInvalid() {
        try {
            Order.buildFrom("10000,H,98,25500", NOW);
            fail();
        } catch (RuntimeException ex) {
            assertEquals("Invalid order side", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenAtLeastOneFieldIsMissing() {
        try {
            Order.buildFrom("10000,H,25500", NOW);
            fail();
        } catch (RuntimeException ex) {
            assertEquals("Incomplete input", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenIdIsNull() {
        try {
            Order.buildFrom(",S,98,25500", NOW);
            fail();
        } catch (RuntimeException ex) {
            assertEquals("Order Id is required", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenSideIsNull() {
        try {
            Order.buildFrom("1000,,98,25500", NOW);
            fail();
        } catch (RuntimeException ex) {
            assertEquals("Side is required", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenPriceIsNull() {
        try {
            Order.buildFrom("1000,S,,25500", NOW);
            fail();
        } catch (RuntimeException ex) {
            assertEquals("Price is required", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenQuantityIsNull() {
        try {
            Order.buildFrom("1000,S,98,", NOW);
            fail();
        } catch (RuntimeException ex) {
            assertEquals("Incomplete input", ex.getMessage());
        }
    }

}
