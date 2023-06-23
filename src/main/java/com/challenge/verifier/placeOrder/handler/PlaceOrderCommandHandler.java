package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.domain.EventPersistentModel;
import com.challenge.verifier.placeOrder.domain.EventType;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderCommandHandler {

    private OrderPlacedPublisher publisher;
    private OrderRepository orderRepository;
    private Logger logger = Logger.getLogger(PlaceOrderCommandHandler.class);


    public PlaceOrderCommandHandler(OrderPlacedPublisher publisher, OrderRepository orderRepository) {
        this.publisher = publisher;
        this.orderRepository = orderRepository;
    }

    public void place(Order order) {
        var event = Event.with(order, EventType.ORDER_PLACED);
        var eventPersistentModel = event.asPersistentModel();
        if (wasOrderAlreadyPlaced(eventPersistentModel)) {
            logger.info("Order " + order.id() + " already placed");
            return;
        }
        var publisherResult = publisher.publish(event);
        if (publisherResult.succeeded()) orderRepository.saveAndFlush(eventPersistentModel);
    }

    private boolean wasOrderAlreadyPlaced(EventPersistentModel eventPersistentModel) {
        return orderRepository.existsById(eventPersistentModel.getId());
    }
}
