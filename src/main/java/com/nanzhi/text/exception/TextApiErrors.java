package com.nanzhi.text.exception;

import com.nanzhi.text.exception.TextExceptions.InvalidTextException;
import com.nanzhi.text.exception.TextExceptions.TextMissingException;
import com.nanzhi.text.exception.TextExceptions.TextStorageException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TextApiErrors {
    @ExceptionHandler(InvalidTextException.class)
    public ResponseEntity<Map<String, String>> invalid(InvalidTextException error) {
        return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(TextMissingException.class)
    public ResponseEntity<Map<String, String>> missing(TextMissingException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(TextStorageException.class)
    public ResponseEntity<Map<String, String>> storage(TextStorageException error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "文字记录操作失败，请稍后重试"));
    }
}
