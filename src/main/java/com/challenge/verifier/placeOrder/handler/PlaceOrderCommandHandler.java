package com.challenge.verifier.placeOrder.handler;

import com.challenge.verifier.common.domain.Event;
import com.challenge.verifier.common.domain.EventType;
import com.challenge.verifier.common.domain.Id;
import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.time.TimeProvider;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.queryOrder.handler.StoreOrderEventCommandHandler;
import com.challenge.verifier.queryOrder.query.OrderEventQueryService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderCommandHandler {

    private OrderPlacedPublisher publisher;
    private StoreOrderEventCommandHandler storeOrderEventCommandHandler;
    private OrderEventQueryService orderEventQueryService;
    private Logger logger = Logger.getLogger(PlaceOrderCommandHandler.class);
    private TimeProvider timeProvider;

    public PlaceOrderCommandHandler(OrderPlacedPublisher publisher, StoreOrderEventCommandHandler storeOrderEventCommandHandler, OrderEventQueryService orderEventQueryService, TimeProvider timeProvider) {
        this.publisher = publisher;
        this.storeOrderEventCommandHandler = storeOrderEventCommandHandler;
        this.orderEventQueryService = orderEventQueryService;
        this.timeProvider = timeProvider;
    }

    public void place(Order order) {
        if (wasOrderAlreadyPlaced(order.id())) {
            logger.info("Order " + order.id() + " already placed");
            return;
        }
        var publisherResult = publisher.publish(order.asPersistentModel());
        if (publisherResult.succeeded())
            storeOrderEventCommandHandler.store(Event.with(order, EventType.ORDER_PLACED, timeProvider.now()));
    }

    private boolean wasOrderAlreadyPlaced(Id id) {
        return orderEventQueryService.existsById(id.value());
    }
}
