package com.gdschongik.gdsc.domain.email.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(
        @NotBlank(message = "이메일 검증 토큰이 비었습니다.") @Schema(description = "이메일 검증 토큰") String token) {}
