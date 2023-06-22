package com.challenge.verifier.placeOrder.ports;

import com.challenge.verifier.placeOrder.domain.EventPersistentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<EventPersistentModel, Long> {
    EventPersistentModel saveAndFlush(EventPersistentModel event);
}
