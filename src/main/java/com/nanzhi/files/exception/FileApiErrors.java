package com.nanzhi.files.exception;

import java.util.Map;
import com.nanzhi.files.exception.FileExceptions.FileMissingException;
import com.nanzhi.files.exception.FileExceptions.FileTooLargeException;
import com.nanzhi.files.exception.FileExceptions.InvalidFileException;
import com.nanzhi.files.exception.FileExceptions.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class FileApiErrors {
    @ExceptionHandler({InvalidFileException.class, MissingServletRequestPartException.class})
    public ResponseEntity<Map<String, String>> invalid(Exception error) {
        return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler({FileTooLargeException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<Map<String, String>> tooLarge(Exception error) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "文件超过上传上限"));
    }

    @ExceptionHandler(FileMissingException.class)
    public ResponseEntity<Map<String, String>> missing(Exception error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler({StorageException.class, java.net.MalformedURLException.class})
    public ResponseEntity<Map<String, String>> storage(Exception error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "文件操作失败，请稍后重试"));
    }
}
