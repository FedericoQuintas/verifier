package com.challenge.verifier.placeOrder.domain;

import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventTest {

    private Order order;
    private Instant now;

    @BeforeEach
    public void before() {
        order = TestOrderBuilder.buildOrder();
        now = Instant.now();
    }

    @Test
    public void convertsToPersistentModel() {
        EventPersistentModel eventPersistentModel = new Event(order, EventType.ORDER_PLACED, now).asPersistentModel();
        assertEquals(eventPersistentModel.getId(), 10000L);
        assertEquals(eventPersistentModel.getSide(), Side.BUY.name());
        assertEquals(eventPersistentModel.getPrice(), BigDecimal.valueOf(98));
        assertEquals(eventPersistentModel.getQuantity(), 25500);
        assertEquals(eventPersistentModel.getEvent_type(), EventType.ORDER_PLACED.name());
        assertEquals(eventPersistentModel.getEvent_time(), now);
    }

    @Test
    public void isOrderFilled() {
        assertTrue(new Event(order, EventType.ORDER_FILLED, now).asPersistentModel().isOrderFilled());
        assertFalse(new Event(order, EventType.ORDER_PLACED, now).asPersistentModel().isOrderFilled());
    }
}
