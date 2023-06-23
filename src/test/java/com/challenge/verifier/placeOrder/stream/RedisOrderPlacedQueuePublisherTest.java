package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.helper.OrderTestHelper;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("redis-test")
public class RedisOrderPlacedQueuePublisherTest {

    @Autowired
    OrderPlacedPublisher publisher;

    @Autowired
    MessageListener listener;

    @Test
    void publishesAndConsumesOrderPlaceEvent() throws InterruptedException {
        OrderPersistentModel orderPersistentModel = OrderTestHelper.buildOrder().asPersistentModel();
        Long timestamp = orderPersistentModel.getTimestamp();
        publisher.publish(orderPersistentModel);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        Thread.sleep(400);
        verify(listener).onMessage(captor.capture(), any());
        assertEquals("{\"id\":10000,\"side\":\"BUY\",\"quantity\":25500,\"price\":98,\"timestamp\":" + timestamp + ",\"onBuySide\":true}", captor.getValue().toString());
    }
}
