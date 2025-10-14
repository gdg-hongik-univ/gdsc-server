package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AfterPartyAttendanceResponse(
        @Schema(description = "총 뒤풀이 참석 인원") Long totalAttendeesCount,
        @Schema(description = "신청 후 참석 인원") Long attendedAfterApplyingCount,
        @Schema(description = "신청 후 미참석 인원") Long notAttendedAfterApplyingCount,
        @Schema(description = "현장 신청 인원") Long onSiteApplicationCount,
        List<EventParticipationDto> eventParticipationDtos) {

    public static AfterPartyAttendanceResponse of(
            Long attendedAfterApplyingCount,
            Long notAttendedAfterApplyingCount,
            Long onSiteApplicationCount,
            List<EventParticipationDto> eventParticipationDtos) {
        return new AfterPartyAttendanceResponse(
                attendedAfterApplyingCount + onSiteApplicationCount,
                attendedAfterApplyingCount,
                notAttendedAfterApplyingCount,
                onSiteApplicationCount,
                eventParticipationDtos);
    }
}
