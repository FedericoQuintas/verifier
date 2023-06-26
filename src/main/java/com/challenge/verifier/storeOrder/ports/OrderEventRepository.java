package com.challenge.verifier.storeOrder.ports;

import com.challenge.verifier.common.domain.EventPersistentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderEventRepository extends JpaRepository<EventPersistentModel, Long> {

}
