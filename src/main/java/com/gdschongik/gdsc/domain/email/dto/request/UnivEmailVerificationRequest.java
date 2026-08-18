package com.gdschongik.gdsc.domain.email.dto.request;

import static com.gdschongik.gdsc.global.common.constant.RegexConstant.VERIFICATION_CODE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UnivEmailVerificationRequest(
        @NotBlank(message = "이메일 인증 코드가 비었습니다.")
                @Pattern(regexp = VERIFICATION_CODE, message = "이메일 인증 코드는 6자리 숫자여야 합니다.")
                @Schema(description = "이메일 인증 코드", pattern = VERIFICATION_CODE)
                String code) {}
