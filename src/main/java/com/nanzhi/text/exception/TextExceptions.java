package com.nanzhi.text.exception;

public final class TextExceptions {
    private TextExceptions() {
    }

    public static class InvalidTextException extends RuntimeException {
        public InvalidTextException(String message) {
            super(message);
        }
    }

    public static class TextMissingException extends RuntimeException {
        public TextMissingException(String message) {
            super(message);
        }
    }

    public static class TextStorageException extends RuntimeException {
        public TextStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
