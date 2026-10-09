package com.nanzhi.network.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.nanzhi.network.exception.NetworkExceptions.QrCodeGenerationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {
    private static final int IMAGE_SIZE = 256;

    public byte[] generate(String content) {
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name(),
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN, 1);
        try {
            BitMatrix matrix = new QRCodeWriter().encode(
                    content, BarcodeFormat.QR_CODE, IMAGE_SIZE, IMAGE_SIZE, hints);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", output);
                return output.toByteArray();
            }
        } catch (WriterException | IOException e) {
            throw new QrCodeGenerationException("无法生成二维码", e);
        }
    }
}
