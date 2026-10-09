package com.nanzhi.files.exception;

public final class FileExceptions {
    private FileExceptions() {
    }

    public static class InvalidFileException extends RuntimeException {
        public InvalidFileException(String message) { super(message); }
    }

    public static class FileTooLargeException extends RuntimeException {
        public FileTooLargeException(String message) { super(message); }
    }

    public static class FileMissingException extends RuntimeException {
        public FileMissingException(String message) { super(message); }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) { super(message, cause); }
    }
}
