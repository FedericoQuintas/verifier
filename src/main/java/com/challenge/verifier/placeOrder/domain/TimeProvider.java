package com.challenge.verifier.placeOrder.domain;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TimeProvider {
    public Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}
