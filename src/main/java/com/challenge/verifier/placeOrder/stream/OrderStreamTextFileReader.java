package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.TimeProvider;
import com.challenge.verifier.placeOrder.handler.PlaceOrderCommandHandler;
import com.challenge.verifier.placeOrder.ports.OrderStreamReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.log4j.Logger;

@Service
public class OrderStreamTextFileReader implements OrderStreamReader {

    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private TimeProvider timeProvider;
    private Logger logger;

    public OrderStreamTextFileReader(PlaceOrderCommandHandler placeOrderCommandHandler, TimeProvider timeProvider) {
        this.placeOrderCommandHandler = placeOrderCommandHandler;
        this.timeProvider = timeProvider;
        logger = Logger.getLogger(OrderStreamTextFileReader.class);
    }

    public void read(String textFile) throws IOException {
        Path path = Paths.get(textFile);

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEachOrdered(line -> {
                logger.info("Reads line " + line);
                placeOrderCommandHandler.place(Order.buildFrom(line, timeProvider.now()));
            });
        }
    }
}
