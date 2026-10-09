package com.nanzhi.text.controller;

import com.nanzhi.text.model.CreateTextRequest;
import com.nanzhi.text.model.StoredText;
import com.nanzhi.text.service.TextStorageService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/texts")
public class TextController {
    private final TextStorageService storage;

    public TextController(TextStorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    public ResponseEntity<StoredText> create(@RequestBody CreateTextRequest request) {
        String content = request == null ? null : request.content();
        return ResponseEntity.status(201).body(storage.save(content));
    }

    @GetMapping
    public List<StoredText> list() {
        return storage.list();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        storage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
