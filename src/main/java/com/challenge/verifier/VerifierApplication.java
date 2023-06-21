package com.challenge.verifier;

import com.challenge.verifier.placeOrder.ports.OrderStreamReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class VerifierApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(VerifierApplication.class, args);
        ApplicationContext ctx = new AnnotationConfigApplicationContext(VerifierApplication.class);
        OrderStreamReader streamReader = ctx.getBean(OrderStreamReader.class);
		streamReader.read(args[0]);
    }

}
