package com.challenge.verifier.matchOrder;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.*;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchOrderCommandHandler {

    private Logger logger = Logger.getLogger(MatchOrderCommandHandler.class);
    private OrdersPriorityQueue ordersPriorityQueue;
    private OrderRepository orderRepository;
    private TimeProvider timeProvider;

    public MatchOrderCommandHandler(OrdersPriorityQueue ordersPriorityQueue, OrderRepository orderRepository, TimeProvider timeProvider) {
        this.ordersPriorityQueue = ordersPriorityQueue;
        this.orderRepository = orderRepository;
        this.timeProvider = timeProvider;
    }

    public void match(Order order) {
        List<EventPersistentModel> orderEvents = orderRepository.findAllById(List.of(order.id().value()));
        if (Event.wasAlreadyProcessed(orderEvents)) {
            logger.info("Order " + order.id().value() + " was already processed");
            return;
        }
        Side matchingSide = order.isOnBuySide() ? Side.SELL : Side.BUY;
        boolean matchingComplete = false;
        while (!matchingComplete) {
            ReadQueueResult readQueueResult = ordersPriorityQueue.readFrom(matchingSide);
            if (readQueueResult.isEmpty() || !readQueueResult.succeeded()) {
                logger.info("It didn't find result for " + order.id().value());
                addToPriorityQueue(order);
                return;
            }
            Order matchingOrder = readQueueResult.order();
            if (pricesMatch(order, matchingOrder)) {
                logger.info("Order " + order.id().value() + " matches order " + matchingOrder.id().value());
                Quantity matchingQuantity = calculateMatchingQuantity(order, matchingOrder);
                matchingOrder = matchingOrder.reduceQuantity(matchingQuantity);
                order = order.reduceQuantity(matchingQuantity);
                matchingComplete = updateOrders(order, matchingOrder);
            } else {
                returnOrdersToPriorityQueue(order, matchingOrder);
                return;
            }
        }
    }

    private static Quantity calculateMatchingQuantity(Order order, Order matchingOrder) {
        return Quantity.of(Math.min(order.quantity().value(), matchingOrder.quantity().value()));
    }

    private boolean updateOrders(Order order, Order matchingOrder) {
        boolean matchingComplete = false;
        if (order.hasRemainingQuantity()) {
            orderRepository.saveAndFlush(buildEvent(order, EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
        } else {
            orderRepository.saveAndFlush(buildEvent(order, EventType.ORDER_FILLED).asPersistentModel());
            matchingComplete = true;
        }
        if (matchingOrder.hasRemainingQuantity()) {
            orderRepository.saveAndFlush(buildEvent(matchingOrder, EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
            addToPriorityQueue(matchingOrder);
        } else {
            orderRepository.saveAndFlush(buildEvent(matchingOrder, EventType.ORDER_FILLED).asPersistentModel());
        }
        return matchingComplete;
    }

    private void returnOrdersToPriorityQueue(Order order, Order matchingOrder) {
        addToPriorityQueue(order);
        addToPriorityQueue(matchingOrder);
    }

    private void addToPriorityQueue(Order order) {
        logger.debug("Order " + order.id().value() + " will be added to the priority queue");
        ordersPriorityQueue.add(order.asPersistentModel());
    }

    private static boolean pricesMatch(Order order, Order matchingOrder) {
        if (order.isOnBuySide()) {
            return order.price().isEqualOrGreaterThan(matchingOrder.price());
        } else {
            return matchingOrder.price().isEqualOrGreaterThan(order.price());
        }
    }

    private Event buildEvent(Order order, EventType eventType) {
        return Event.with(order, eventType, timeProvider.now());
    }
}
