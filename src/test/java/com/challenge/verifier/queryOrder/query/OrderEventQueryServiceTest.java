package com.challenge.verifier.queryOrder.query;

import com.challenge.verifier.queryOrder.ports.OrderEventRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OrderEventQueryServiceTest {

    private OrderEventRepository orderEventRepository;
    private OrderEventQueryService orderEventQueryService;

    @Test
    public void bridgesExistsById() {
        orderEventRepository = mock(OrderEventRepository.class);
        orderEventQueryService = new OrderEventQueryService(orderEventRepository);
        orderEventQueryService.existsById(1L);
        verify(orderEventRepository).existsById(1L);
    }

    @Test
    public void bridgesFindAllById() {
        orderEventRepository = mock(OrderEventRepository.class);
        orderEventQueryService = new OrderEventQueryService(orderEventRepository);
        orderEventQueryService.findAllById(List.of(10L));
        verify(orderEventRepository).findAllById(List.of(10L));
    }
}
