package com.nanzhi.network.exception;

import com.nanzhi.network.exception.NetworkExceptions.InvalidNetworkAddressException;
import com.nanzhi.network.exception.NetworkExceptions.QrCodeGenerationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NetworkApiErrors {
    @ExceptionHandler(InvalidNetworkAddressException.class)
    public ResponseEntity<Map<String, String>> invalid(InvalidNetworkAddressException error) {
        return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(QrCodeGenerationException.class)
    public ResponseEntity<Map<String, String>> generation(QrCodeGenerationException error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "二维码生成失败，请稍后重试"));
    }
}
