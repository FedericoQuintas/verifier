package com.challenge.verifier.placeOrder.stream;

public class PublisherResult {

    private boolean succeeded;

    private PublisherResult(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public static PublisherResult ok() {
        return new PublisherResult(true);
    }

    public static PublisherResult error() {
        return new PublisherResult(false);
    }

    public boolean succeeded() {
        return succeeded;
    }
}
