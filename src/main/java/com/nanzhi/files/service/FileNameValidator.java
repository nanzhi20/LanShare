package com.nanzhi.files.service;

import com.nanzhi.files.exception.FileExceptions.InvalidFileException;
import java.util.regex.Pattern;

final class FileNameValidator {
    private static final Pattern WINDOWS_RESERVED = Pattern.compile(
            "(?i)^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$");

    private FileNameValidator() {
    }

    static String validate(String name) {
        if (name == null || name.isBlank() || name.equals(".") || name.equals("..")
                || name.endsWith(".") || name.endsWith(" ") || name.length() > 180
                || WINDOWS_RESERVED.matcher(name).matches()) {
            throw new InvalidFileException("文件名无效");
        }
        for (char c : name.toCharArray()) {
            if (c < 32 || "<>:\"/\\|?*".indexOf(c) >= 0) {
                throw new InvalidFileException("文件名包含不安全字符");
            }
        }
        return name;
    }
}
