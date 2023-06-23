package com.challenge.verifier.placeOrder.persistence;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.domain.EventPersistentModel;
import com.challenge.verifier.placeOrder.domain.EventType;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.helper.OrderTestHelper;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    OrderRepository repository;

    @Test
    public void storesOrder() {
        Order order = OrderTestHelper.buildOrder();
        EventPersistentModel event = Event.with(order, EventType.ORDER_PLACED).asPersistentModel();
        repository.saveAndFlush(event);
        Optional<EventPersistentModel> eventOptional = repository.findById(event.getId());
        assertEquals(eventOptional.get().getId(), event.getId());
    }
}