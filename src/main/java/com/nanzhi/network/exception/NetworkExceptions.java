package com.nanzhi.network.exception;

public final class NetworkExceptions {
    private NetworkExceptions() {
    }

    public static class InvalidNetworkAddressException extends RuntimeException {
        public InvalidNetworkAddressException(String message) {
            super(message);
        }
    }

    public static class QrCodeGenerationException extends RuntimeException {
        public QrCodeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
