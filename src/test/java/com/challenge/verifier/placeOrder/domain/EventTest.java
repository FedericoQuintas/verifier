package com.challenge.verifier.placeOrder.domain;

import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventTest {

    @Test
    public void convertsToPersistentModel() {
        Order order = TestOrderBuilder.buildOrder();
        EventPersistentModel eventPersistentModel = new Event(order, EventType.ORDER_PLACED).asPersistentModel();
        assertEquals(eventPersistentModel.getId(), 10000L);
        assertEquals(eventPersistentModel.getSide(), Side.BUY.name());
        assertEquals(eventPersistentModel.getPrice(), BigDecimal.valueOf(98));
        assertEquals(eventPersistentModel.getQuantity(), 25500);
        assertEquals(eventPersistentModel.getEvent_type(), EventType.ORDER_PLACED.name());
        assertEquals(eventPersistentModel.getEvent_time(), order.timestamp());
    }
}
