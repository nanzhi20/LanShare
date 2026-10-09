package com.nanzhi.network;

import com.nanzhi.network.service.QrCodeService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QrCodeManualCheck {
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };

    public static void main(String[] args) throws IOException {
        String url = "http://192.168.1.20:8080/";
        byte[] image = new QrCodeService().generate(url);
        require(hasPngSignature(image), "生成结果不是有效的 PNG 文件");

        Path output = Files.createTempFile("lanshare-qr-check-", ".png");
        Files.write(output, image);
        System.out.println("二维码生成检查通过");
        System.out.println("二维码内容：" + url);
        System.out.println("图片位置：" + output);
        System.out.println("请用手机扫码，确认能够识别出上述网址；该示例地址不要求实际可访问。");
    }

    private static boolean hasPngSignature(byte[] image) {
        if (image.length < PNG_SIGNATURE.length) {
            return false;
        }
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (image[i] != PNG_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
