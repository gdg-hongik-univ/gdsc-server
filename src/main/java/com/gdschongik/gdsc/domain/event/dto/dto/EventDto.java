package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import java.time.LocalDateTime;

public record EventDto(
        Long eventId,
        String name,
        String venue,
        LocalDateTime startAt,
        String description,
        Period applicationPeriod,
        UsageStatus regularRoleOnlyStatus,
        UsageStatus afterPartyStatus,
        UsageStatus prePaymentStatus,
        UsageStatus postPaymentStatus,
        UsageStatus rsvpQuestionStatus,
        UsageStatus noticeConfirmQuestionStatus,
        Integer mainEventMaxApplicantCount,
        Integer afterPartyMaxApplicantCount) {

    public static EventDto from(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getVenue(),
                event.getStartAt(),
                event.getDescription(),
                event.getApplicationPeriod(),
                event.getRegularRoleOnlyStatus(),
                event.getAfterPartyStatus(),
                event.getPrePaymentStatus(),
                event.getPostPaymentStatus(),
                event.getRsvpQuestionStatus(),
                event.getNoticeConfirmQuestionStatus(),
                event.getMainEventMaxApplicantCount(),
                event.getAfterPartyMaxApplicantCount());
    }
}
