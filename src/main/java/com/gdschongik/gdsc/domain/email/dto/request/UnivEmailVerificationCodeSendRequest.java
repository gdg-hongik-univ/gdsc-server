package com.gdschongik.gdsc.domain.email.dto.request;

import static com.gdschongik.gdsc.global.common.constant.RegexConstant.HONGIK_EMAIL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UnivEmailVerificationCodeSendRequest(
        @NotBlank
                @Pattern(regexp = HONGIK_EMAIL, message = "학교 이메일은 " + HONGIK_EMAIL + " 형식이어야 합니다.")
                @Schema(description = "학교 이메일", pattern = HONGIK_EMAIL)
                String univEmail) {}
