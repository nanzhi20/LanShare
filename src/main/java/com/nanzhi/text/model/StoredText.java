package com.nanzhi.text.model;

import java.time.Instant;

public record StoredText(String id, String content, Instant createdAt) {
}
