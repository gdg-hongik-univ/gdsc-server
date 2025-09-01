package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.event.dto.dto.AfterPartyApplicantCountDto;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipationDto;
import org.springframework.data.domain.Page;

public record AfterPartyApplicantResponse(Page<EventParticipationDto> applicants, AfterPartyApplicantCountDto counts) {

    public static AfterPartyApplicantResponse of(
            Page<EventParticipationDto> applicants, AfterPartyApplicantCountDto counts) {
        return new AfterPartyApplicantResponse(applicants, counts);
    }
}
