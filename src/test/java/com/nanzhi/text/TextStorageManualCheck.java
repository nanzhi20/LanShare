package com.nanzhi.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanzhi.text.exception.TextExceptions.InvalidTextException;
import com.nanzhi.text.model.StoredText;
import com.nanzhi.text.service.TextStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class TextStorageManualCheck {
    public static void main(String[] args) throws IOException {
        Path directory = Files.createTempDirectory("lanshare-text-check-");
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try {
            TextStorageService storage = new TextStorageService(directory.toString(), objectMapper);
            expectInvalid(() -> storage.save("   "), "全空白文字应被拒绝");
            expectInvalid(() -> storage.save("a".repeat(TextStorageService.MAX_CONTENT_LENGTH + 1)),
                    "超长文字应被拒绝");
            StoredText first = storage.save("第一条文字");
            storage.save("第二条文字\n保留换行");

            List<StoredText> beforeRestart = storage.list();
            require(beforeRestart.size() == 2, "应保存两条记录");

            TextStorageService restarted = new TextStorageService(directory.toString(), objectMapper);
            require(restarted.list().size() == 2, "重新创建服务后记录应继续存在");

            restarted.delete(first.id());
            require(restarted.list().size() == 1, "删除后应只剩一条记录");

            System.out.println("文字存储手动检查通过");
            System.out.println("临时检查目录：" + directory);
        } finally {
            deleteRecursively(directory);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void expectInvalid(Runnable action, String message) {
        try {
            action.run();
            throw new IllegalStateException(message);
        } catch (InvalidTextException expected) {
            System.out.println("符合预期的拒绝：" + expected.getMessage());
        }
    }

    private static void deleteRecursively(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
