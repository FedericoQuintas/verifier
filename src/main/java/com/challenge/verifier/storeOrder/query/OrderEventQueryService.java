package com.challenge.verifier.storeOrder.query;

import com.challenge.verifier.common.domain.EventPersistentModel;
import com.challenge.verifier.storeOrder.ports.OrderEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderEventQueryService {

    private OrderEventRepository orderEventRepository;

    public OrderEventQueryService(OrderEventRepository orderEventRepository) {
        this.orderEventRepository = orderEventRepository;
    }

    public boolean existsById(Long id) {
        return orderEventRepository.existsById(id);
    }

    public List<EventPersistentModel> findAllById(List<Long> ids) {
        return orderEventRepository.findAllById(ids);
    }
}
