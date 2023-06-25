package com.challenge.verifier.reconcileOrderBook.domain;

public record ReconciliationResult(String output, boolean succeeded) {
    public static ReconciliationResult withOutput(String output) {
        return new ReconciliationResult(output, true);
    }

    public static ReconciliationResult withError(String message) {
        return new ReconciliationResult(message, false);
    }
}
