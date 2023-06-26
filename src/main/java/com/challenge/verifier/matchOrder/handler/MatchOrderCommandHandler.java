package com.challenge.verifier.matchOrder.handler;

import com.challenge.verifier.common.domain.*;
import com.challenge.verifier.common.time.TimeProvider;
import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.matchOrder.ports.TradesLogWriter;
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
    private TradesLogWriter tradesLogWriter;

    public MatchOrderCommandHandler(OrdersPriorityQueue ordersPriorityQueue, OrderRepository orderRepository,
                                    TimeProvider timeProvider, TradesLogWriter tradesLogWriter) {
        this.ordersPriorityQueue = ordersPriorityQueue;
        this.orderRepository = orderRepository;
        this.timeProvider = timeProvider;
        this.tradesLogWriter = tradesLogWriter;
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
                appendToTradeLog(order, matchingOrder, matchingQuantity);
            } else {
                returnOrdersToPriorityQueue(order, matchingOrder);
                return;
            }
        }
    }

    private void appendToTradeLog(Order order, Order matchingOrder, Quantity matchingQuantity) {
        String log = "trade " + order.id().value() + "," + matchingOrder.id().value() + "," + matchingOrder.price().value() + "," + matchingQuantity.value();
        tradesLogWriter.append(log);
    }

    private static Quantity calculateMatchingQuantity(Order order, Order matchingOrder) {
        return Quantity.of(Math.min(order.quantity().value(), matchingOrder.quantity().value()));
    }

    private boolean updateOrders(Order order, Order matchingOrder) {
        boolean matchingComplete = false;
        if (order.hasRemainingQuantity()) {
            orderRepository.save(buildEvent(order, EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
        } else {
            orderRepository.save(buildEvent(order, EventType.ORDER_FILLED).asPersistentModel());
            matchingComplete = true;
        }
        if (matchingOrder.hasRemainingQuantity()) {
            orderRepository.save(buildEvent(matchingOrder, EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
            addToPriorityQueue(matchingOrder);
        } else {
            orderRepository.save(buildEvent(matchingOrder, EventType.ORDER_FILLED).asPersistentModel());
        }
        return matchingComplete;
    }

    private void returnOrdersToPriorityQueue(Order order, Order matchingOrder) {
        addToPriorityQueue(order);
        addToPriorityQueue(matchingOrder);
    }

    private void addToPriorityQueue(Order order) {
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
