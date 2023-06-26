package com.challenge.verifier.queryOrder.ports;

import com.challenge.verifier.common.domain.EventPersistentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderEventRepository extends JpaRepository<EventPersistentModel, Long> {

}
