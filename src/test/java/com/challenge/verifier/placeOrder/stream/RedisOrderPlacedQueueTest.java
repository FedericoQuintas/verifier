package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.common.domain.*;
import com.challenge.verifier.matchOrder.handler.MatchOrderCommandHandler;
import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("redis-test")
public class RedisOrderPlacedQueueTest {

    public static final int MILLIS = 400;
    public static final Price PRICE = Price.of(98);
    public static final Quantity QUANTITY = Quantity.of(25500);
    public static final Id ID = Id.of(10000L);

    @Autowired
    OrderPlacedPublisher publisher;

    @Autowired
    MatchOrderCommandHandler matchOrderCommandHandler;

    @Test
    void publishesAndConsumesOrderPlaceEvent() throws InterruptedException {
        OrderPersistentModel orderPersistentModel = TestOrderBuilder.buildOrder().asPersistentModel();
        long timestamp = orderPersistentModel.getTimestamp();
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

        publisher.publish(orderPersistentModel);
        waitForMessageToBeConsumed();

        verify(matchOrderCommandHandler).match(captor.capture());
        assertEquals(ID, captor.getValue().id());
        assertEquals(QUANTITY, captor.getValue().quantity());
        assertEquals(Instant.ofEpochMilli(timestamp), captor.getValue().timestamp());
        assertEquals(Side.BUY, captor.getValue().side());
        assertEquals(PRICE, captor.getValue().price());
    }

    private static void waitForMessageToBeConsumed() throws InterruptedException {
        Thread.sleep(MILLIS);
    }
}
