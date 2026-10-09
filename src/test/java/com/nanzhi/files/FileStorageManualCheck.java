package com.nanzhi.files;

import com.nanzhi.files.model.StoredFile;
import com.nanzhi.files.service.FileStorageService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

/** Run this main method in IDEA; it uses a temporary folder instead of the real uploads folder. */
public class FileStorageManualCheck {
    public static void main(String[] args) throws Exception {
        Path temporaryDirectory = Files.createTempDirectory("lanshare-manual-check-");
        MultipartProperties multipart = new MultipartProperties();
        multipart.setMaxFileSize(DataSize.ofMegabytes(100));
        FileStorageService storage = new FileStorageService(temporaryDirectory.toString(), multipart);

        StoredFile first = storage.save(sample("example.txt", "第一个文件"));
        StoredFile second = storage.save(sample("example.txt", "同名的第二个文件"));
        System.out.println("临时存储目录：" + temporaryDirectory);
        System.out.println("上传后文件数量（预期 2）：" + storage.list().size());
        System.out.println("两个同名文件的 ID 不同：" + !first.id().equals(second.id()));
        System.out.println("第一个文件内容：" + Files.readString(storage.find(first.id()).path()));

        FileStorageService restarted = new FileStorageService(temporaryDirectory.toString(), multipart);
        System.out.println("重新创建服务对象后文件数量（预期 2）：" + restarted.list().size());
        restarted.delete(first.id());
        System.out.println("删除一个文件后数量（预期 1）：" + restarted.list().size());
        System.out.println("请检查上面的结果；临时目录保留供你自行查看和清理。");
    }

    private static MockMultipartFile sample(String name, String contents) {
        return new MockMultipartFile("file", name, "text/plain",
                contents.getBytes(StandardCharsets.UTF_8));
    }
}
