package com.challenge.verifier.placeOrder.persistence;

import com.challenge.verifier.common.domain.Event;
import com.challenge.verifier.common.domain.EventPersistentModel;
import com.challenge.verifier.common.domain.EventType;
import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.placeOrder.helper.TestOrderBuilder;
import com.challenge.verifier.storeOrder.ports.OrderEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@DataJpaTest
public class OrderEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    OrderEventRepository repository;

    @Test
    public void storesAndFindsOrder() {
        Order order = TestOrderBuilder.buildOrder();
        EventPersistentModel event = Event.with(order, EventType.ORDER_PLACED, Instant.now()).asPersistentModel();
        repository.save(event);
        Optional<EventPersistentModel> eventOptional = repository.findById(event.getId());
        assertEquals(eventOptional.get().getId(), event.getId());
        List<EventPersistentModel> eventsByOrderId = repository.findAllById(List.of(event.getId()));
        assertEquals(1, eventsByOrderId.size());
    }
}
