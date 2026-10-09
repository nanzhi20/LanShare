package com.nanzhi.files.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.nanzhi.files.exception.FileExceptions.FileMissingException;
import com.nanzhi.files.exception.FileExceptions.FileTooLargeException;
import com.nanzhi.files.exception.FileExceptions.InvalidFileException;
import com.nanzhi.files.exception.FileExceptions.StorageException;
import com.nanzhi.files.model.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path storageDirectory;
    private final long maxFileSize;

    public FileStorageService(@Value("${lanshare.storage-dir:uploads}") String storageDir,
                              MultipartProperties multipartProperties) {
        this.storageDirectory = Path.of(storageDir).toAbsolutePath().normalize();
        this.maxFileSize = multipartProperties.getMaxFileSize().toBytes();
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new StorageException("无法创建文件存储目录", e);
        }
    }

    public long maxFileSize() {
        return maxFileSize;
    }

    public String storagePath() {
        return storageDirectory.toString();
    }

    public StoredFile save(MultipartFile upload) {
        if (upload == null || upload.isEmpty()) {
            throw new InvalidFileException("请选择非空文件");
        }
        if (upload.getSize() > maxFileSize) {
            throw new FileTooLargeException("文件超过上传上限");
        }
        String name = FileNameValidator.validate(upload.getOriginalFilename());
        String id = UUID.randomUUID().toString();
        Path destination = storageDirectory.resolve(id + "__" + name).normalize();
        Path temporary = storageDirectory.resolve(id + ".part");
        try {
            try (InputStream input = upload.getInputStream();
                 OutputStream output = Files.newOutputStream(temporary,
                         StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                byte[] buffer = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > maxFileSize) {
                        throw new FileTooLargeException("文件超过上传上限");
                    }
                    output.write(buffer, 0, count);
                }
            }
            Files.move(temporary, destination);
            return describe(destination);
        } catch (FileTooLargeException e) {
            throw e;
        } catch (IOException e) {
            throw new StorageException("保存文件失败", e);
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // A failed cleanup must not hide the original upload error.
            }
        }
    }

    public List<StoredFile> list() {
        List<StoredFile> files = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(storageDirectory)) {
            for (Path entry : entries) {
                if (Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)
                        && isStoredName(entry.getFileName().toString())) {
                    files.add(describe(entry));
                }
            }
        } catch (IOException e) {
            throw new StorageException("读取文件列表失败", e);
        }
        files.sort(Comparator.comparing(StoredFile::uploadedAt).reversed());
        return files;
    }

    public StoredDownload find(String id) {
        UUID parsed = parseId(id);
        String prefix = parsed + "__";
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(storageDirectory, prefix + "*")) {
            for (Path entry : entries) {
                if (Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)
                        && isStoredName(entry.getFileName().toString())) {
                    return new StoredDownload(describe(entry), entry);
                }
            }
        } catch (IOException e) {
            throw new StorageException("读取文件失败", e);
        }
        throw new FileMissingException("文件不存在");
    }

    public void delete(String id) {
        Path file = find(id).path();
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new StorageException("删除文件失败", e);
        }
    }

    private StoredFile describe(Path file) {
        String storedName = file.getFileName().toString();
        int separator = storedName.indexOf("__");
        try {
            Instant uploadedAt = Files.getLastModifiedTime(file).toInstant();
            return new StoredFile(storedName.substring(0, separator), storedName.substring(separator + 2),
                    Files.size(file), uploadedAt);
        } catch (IOException e) {
            throw new StorageException("读取文件信息失败", e);
        }
    }

    private static boolean isStoredName(String name) {
        if (name.length() < 39 || name.charAt(36) != '_' || name.charAt(37) != '_') {
            return false;
        }
        try {
            UUID.fromString(name.substring(0, 36));
            return !name.substring(38).isBlank();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new FileMissingException("文件不存在");
        }
    }

    public record StoredDownload(StoredFile metadata, Path path) {
    }
}
