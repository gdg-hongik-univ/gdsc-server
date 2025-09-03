package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.common.vo.Period;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record EventUpdateRequest(
        @NotBlank String name,
        String venue,
        @NotNull LocalDateTime startAt,
        @NotBlank String applicationDescription,
        @NotNull Period applicationPeriod,
        @Positive Integer mainEventMaxApplicantCount,
        @Positive Integer afterPartyMaxApplicantCount) {}
