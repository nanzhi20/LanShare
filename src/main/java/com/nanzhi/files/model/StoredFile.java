package com.nanzhi.files.model;

import java.time.Instant;

public record StoredFile(String id, String name, long size, Instant uploadedAt) {
}
