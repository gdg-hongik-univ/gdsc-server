package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record EventCreateRequest(
        @NotBlank String name,
        String venue,
        @NotNull LocalDateTime startAt,
        @NotBlank String applicationDescription,
        @NotNull Period applicationPeriod,
        @NotNull UsageStatus regularRoleOnlyStatus,
        @NotNull UsageStatus afterPartyStatus,
        @NotNull UsageStatus prePaymentStatus,
        @NotNull UsageStatus postPaymentStatus,
        @NotNull UsageStatus rsvpQuestionStatus,
        @NotNull UsageStatus noticeConfirmQuestionStatus,
        @Positive @Schema(description = "본 행사 최대 신청 가능 인원. 제한 없음은 null을 입력합니다.") Integer mainEventMaxApplicantCount,
        @Positive @Schema(description = "뒤풀이 최대 신청 가능 인원. 제한 없음은 null을 입력합니다.") Integer afterPartyMaxApplicantCount) {}
