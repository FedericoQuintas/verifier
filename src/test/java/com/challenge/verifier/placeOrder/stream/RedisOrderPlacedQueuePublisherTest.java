package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.matchOrder.MatchOrderCommandHandler;
import com.challenge.verifier.placeOrder.domain.*;
import com.challenge.verifier.placeOrder.helper.OrderTestHelper;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("redis-test")
public class RedisOrderPlacedQueuePublisherTest {

    public static final int MILLIS = 400;
    public static final Price PRICE = Price.of(BigDecimal.valueOf(98));
    public static final Quantity QUANTITY = Quantity.of(25500);
    public static final Id ID = Id.of(10000L);

    @Autowired
    OrderPlacedPublisher publisher;

    @MockBean
    MatchOrderCommandHandler matchOrderCommandHandler;

    @Test
    void publishesAndConsumesOrderPlaceEvent() throws InterruptedException {
        OrderPersistentModel orderPersistentModel = OrderTestHelper.buildOrder().asPersistentModel();
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
