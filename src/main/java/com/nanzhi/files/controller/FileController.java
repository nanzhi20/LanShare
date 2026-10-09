package com.nanzhi.files.controller;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.nanzhi.files.model.StoredFile;
import com.nanzhi.files.model.TransferConfig;
import com.nanzhi.files.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoredFile> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(storage.save(file));
    }

    @GetMapping
    public List<StoredFile> list() {
        return storage.list();
    }

    @GetMapping("/config")
    public TransferConfig config() {
        return new TransferConfig(storage.maxFileSize(), storage.storagePath());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) throws MalformedURLException {
        FileStorageService.StoredDownload stored = storage.find(id);
        Resource resource = new UrlResource(stored.path().toUri());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(stored.metadata().name(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(stored.metadata().size())
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        storage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
