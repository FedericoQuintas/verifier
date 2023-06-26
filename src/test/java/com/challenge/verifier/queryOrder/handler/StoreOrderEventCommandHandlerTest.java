package com.challenge.verifier.queryOrder.handler;

import com.challenge.verifier.common.domain.Event;
import com.challenge.verifier.common.domain.EventType;
import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.helper.TestOrderBuilder;
import com.challenge.verifier.queryOrder.ports.OrderEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StoreOrderEventCommandHandlerTest {

    private OrderEventRepository orderEventRepository;
    private StoreOrderEventCommandHandler storeOrderEventCommandHandler;

    @Test
    public void storesEventAsPersistentModel() {
        orderEventRepository = mock(OrderEventRepository.class);
        storeOrderEventCommandHandler = new StoreOrderEventCommandHandler(orderEventRepository);
        Order order = TestOrderBuilder.buildOrder();
        Event event = new Event(order, EventType.ORDER_PLACED, Instant.now());
        storeOrderEventCommandHandler.store(event);
        verify(orderEventRepository).save(event.asPersistentModel());
    }
}
