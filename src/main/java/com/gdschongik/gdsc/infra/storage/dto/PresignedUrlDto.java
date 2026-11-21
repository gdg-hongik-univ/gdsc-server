package com.gdschongik.gdsc.infra.storage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PresignedUrlDto(
        String objectKey,
        String url,
        String method,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant expiresAt,
        Map<String, List<String>> requiredHeaders) {

    public PresignedUrlDto {
        requiredHeaders = requiredHeaders.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));
    }
}
