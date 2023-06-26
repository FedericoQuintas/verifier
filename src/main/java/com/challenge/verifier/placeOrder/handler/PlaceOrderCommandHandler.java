package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.common.domain.Event;
import com.challenge.verifier.common.domain.EventType;
import com.challenge.verifier.common.domain.Id;
import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.time.TimeProvider;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderCommandHandler {

    private OrderPlacedPublisher publisher;
    private OrderRepository orderRepository;
    private Logger logger = Logger.getLogger(PlaceOrderCommandHandler.class);
    private TimeProvider timeProvider;

    public PlaceOrderCommandHandler(OrderPlacedPublisher publisher, OrderRepository orderRepository, TimeProvider timeProvider) {
        this.publisher = publisher;
        this.orderRepository = orderRepository;
        this.timeProvider = timeProvider;
    }

    public void place(Order order) {
        if (wasOrderAlreadyPlaced(order.id())) {
            logger.info("Order " + order.id() + " already placed");
            return;
        }
        var publisherResult = publisher.publish(order.asPersistentModel());
        if (publisherResult.succeeded())
            orderRepository.saveAndFlush(Event.with(order, EventType.ORDER_PLACED, timeProvider.now()).asPersistentModel());
    }

    private boolean wasOrderAlreadyPlaced(Id id) {
        return orderRepository.existsById(id.value());
    }
}
