package com.gdschongik.gdsc.domain.study.dto.request;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record StudyCreateRequest(
        @NotNull @Positive Long mentorId,
        @NotNull StudyType type,
        @NotNull String title,
        @NotNull Semester semester,
        @NotNull @Positive Integer totalRound,
        @Nullable DayOfWeek dayOfWeek,
        @Nullable LocalTime startTime,
        @Nullable LocalTime endTime,
        @NotNull Period applicationPeriod,
        String discordChannelId,
        String discordRoleId,
        @NotNull Integer minAssignmentLength) {
    // TODO: 라이브 세션 관련 필드의 경우 VO로 묶기
}
