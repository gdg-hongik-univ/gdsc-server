package com.gdschongik.gdsc.domain.study.dto.request;

import static com.gdschongik.gdsc.global.common.constant.RegexConstant.ATTENDANCE_NUMBER;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AttendanceCreateRequest(
        @NotBlank
                @Pattern(regexp = ATTENDANCE_NUMBER, message = "출석번호는 " + ATTENDANCE_NUMBER + " 형식이어야 합니다.")
                @Schema(description = "출석번호")
                String attendanceNumber) {}
