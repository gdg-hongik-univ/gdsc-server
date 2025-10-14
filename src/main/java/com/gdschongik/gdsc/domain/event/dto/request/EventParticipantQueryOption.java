package com.gdschongik.gdsc.domain.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record EventParticipantQueryOption(
        @Schema(description = "이름") String name,
        @Schema(description = "학번") String studentId,
        @Schema(description = "전화번호") String phone) {}
