package com.challenge.verifier.placeOrder.http;

import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.time.TimeProvider;
import com.challenge.verifier.placeOrder.handler.PlaceOrderCommandHandler;
import com.challenge.verifier.reconcileOrderBook.handler.ReconcileOrderBookCommandHandler;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static java.lang.Thread.sleep;

@RestController
public class HttpFileController {

    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private ReconcileOrderBookCommandHandler reconcileOrderBookCommandHandler;
    private TimeProvider timeProvider;
    private Logger logger = Logger.getLogger(HttpFileController.class);


    public HttpFileController(PlaceOrderCommandHandler placeOrderCommandHandler, ReconcileOrderBookCommandHandler reconcileOrderBookCommandHandler, TimeProvider timeProvider) {
        this.placeOrderCommandHandler = placeOrderCommandHandler;
        this.reconcileOrderBookCommandHandler = reconcileOrderBookCommandHandler;
        this.timeProvider = timeProvider;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/upload")
    public ResponseEntity read(@RequestPart(name = "file") MultipartFile file) throws IOException {
        logger.info("Receives file " + file.getOriginalFilename());
        BufferedReader bufferedReader = setUpBufferReader(file);
        try {
            placeOrders(bufferedReader);
            return ResponseEntity.ok().body(reconcileOrderBookCommandHandler.reconcile());
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            return ResponseEntity.internalServerError().body("Please try again later");
        }
    }

    private static BufferedReader setUpBufferReader(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(in);
        return bufferedReader;
    }

    private void placeOrders(BufferedReader bufferedReader) {
        bufferedReader.lines()
                .forEach(line -> {
                    try {
                        placeOrderCommandHandler.place(Order.buildFrom(line, timeProvider.now()));
                        sleep(200);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage());
                    }
                });
    }
}
