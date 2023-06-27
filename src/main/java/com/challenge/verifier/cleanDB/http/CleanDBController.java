package com.challenge.verifier.cleanDB.http;

import com.challenge.verifier.matchOrder.ports.OrdersPriorityQueue;
import com.challenge.verifier.matchOrder.ports.TradesLogWriter;
import com.challenge.verifier.queryOrder.ports.OrderEventRepository;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CleanDBController {

    private OrderEventRepository orderEventRepository;
    private Logger logger = Logger.getLogger(CleanDBController.class);
    private TradesLogWriter tradesLogWriter;
    private OrdersPriorityQueue ordersPriorityQueue;


    public CleanDBController(OrderEventRepository orderEventRepository, TradesLogWriter tradesLogWriter, OrdersPriorityQueue ordersPriorityQueue) {
        this.orderEventRepository = orderEventRepository;
        this.tradesLogWriter = tradesLogWriter;
        this.ordersPriorityQueue = ordersPriorityQueue;
    }

    /*
        Only added in case it helps reviewers to run test suites.
        It doesn't need auth keys.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/clean")
    public ResponseEntity clean() {
        logger.info("Request to clean DBs");
        try {
            orderEventRepository.deleteAll();
            tradesLogWriter.deleteAll();
            ordersPriorityQueue.deleteAll();
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            return ResponseEntity.internalServerError().body("Please try again later");
        }
    }
}
