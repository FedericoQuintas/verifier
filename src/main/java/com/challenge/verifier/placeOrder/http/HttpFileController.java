package com.challenge.verifier.placeOrder.http;

import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.TimeProvider;
import com.challenge.verifier.placeOrder.handler.PlaceOrderCommandHandler;
import org.apache.log4j.Logger;
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

@RestController
public class HttpFileController {

    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private TimeProvider timeProvider;
    private Logger logger = Logger.getLogger(HttpFileController.class);


    public HttpFileController(PlaceOrderCommandHandler placeOrderCommandHandler, TimeProvider timeProvider) {
        this.placeOrderCommandHandler = placeOrderCommandHandler;
        this.timeProvider = timeProvider;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public void read(@RequestPart(name = "file", required = false) MultipartFile file) throws IOException {
        logger.info("Receives file " + file.getName());
        System.out.println("Receives file " + file.getOriginalFilename());
        InputStream inputStream = file.getInputStream();
        InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(in);
        bufferedReader.lines()
                .forEach(line -> placeOrderCommandHandler.place(Order.buildFrom(line, timeProvider.now())));

    }
}
