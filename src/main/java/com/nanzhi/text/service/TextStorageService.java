package com.nanzhi.text.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanzhi.text.exception.TextExceptions.InvalidTextException;
import com.nanzhi.text.exception.TextExceptions.TextMissingException;
import com.nanzhi.text.exception.TextExceptions.TextStorageException;
import com.nanzhi.text.model.StoredText;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TextStorageService {
    public static final int MAX_CONTENT_LENGTH = 20_000;

    private final Path storageDirectory;
    private final ObjectMapper objectMapper;

    public TextStorageService(@Value("${lanshare.text-storage-dir:data/texts}") String storageDir,
                              ObjectMapper objectMapper) {
        this.storageDirectory = Path.of(storageDir).toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new TextStorageException("无法创建文字存储目录", e);
        }
    }

    public StoredText save(String content) {
        validate(content);
        StoredText text = new StoredText(UUID.randomUUID().toString(), content, Instant.now());
        Path destination = pathFor(text.id());
        Path temporary = storageDirectory.resolve(text.id() + ".part");
        try {
            try (OutputStream output = Files.newOutputStream(temporary,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                objectMapper.writeValue(output, text);
            }
            moveIntoPlace(temporary, destination);
            return text;
        } catch (IOException e) {
            throw new TextStorageException("保存文字记录失败", e);
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // A failed cleanup must not hide the original storage error.
            }
        }
    }

    public List<StoredText> list() {
        List<StoredText> texts = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(storageDirectory, "*.json")) {
            for (Path entry : entries) {
                if (Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)) {
                    texts.add(read(entry));
                }
            }
        } catch (IOException e) {
            throw new TextStorageException("读取文字记录失败", e);
        }
        texts.sort(Comparator.comparing(StoredText::createdAt).reversed());
        return texts;
    }

    public void delete(String id) {
        UUID parsed = parseId(id);
        Path entry = pathFor(parsed.toString());
        try {
            if (!Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)
                    || !Files.deleteIfExists(entry)) {
                throw new TextMissingException("文字记录不存在");
            }
        } catch (TextMissingException e) {
            throw e;
        } catch (IOException e) {
            throw new TextStorageException("删除文字记录失败", e);
        }
    }

    private StoredText read(Path entry) {
        try {
            StoredText text = objectMapper.readValue(entry.toFile(), StoredText.class);
            String fileId = entry.getFileName().toString().replaceFirst("\\.json$", "");
            if (text.id() == null || !text.id().equals(fileId)
                    || text.content() == null || text.createdAt() == null) {
                throw new IOException("文字记录格式无效");
            }
            return text;
        } catch (IOException e) {
            throw new TextStorageException("读取文字记录失败", e);
        }
    }

    private Path pathFor(String id) {
        return storageDirectory.resolve(id + ".json");
    }

    private static void validate(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidTextException("请输入非空文字");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidTextException("文字不能超过 " + MAX_CONTENT_LENGTH + " 个字符");
        }
    }

    private static UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TextMissingException("文字记录不存在");
        }
    }

    private static void moveIntoPlace(Path temporary, Path destination) throws IOException {
        try {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, destination);
        }
    }
}
