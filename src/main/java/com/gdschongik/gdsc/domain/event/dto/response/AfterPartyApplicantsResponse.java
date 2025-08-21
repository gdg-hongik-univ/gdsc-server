package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipableMemberDto;
import org.springframework.data.domain.Page;

public record AfterPartyApplicantsResponse(Page<EventParticipableMemberDto> applicants, CountDto counts) {

    // TODO: CountDto 네이밍 적절하게 변경, 내용 구현, 별도 DTO로 분리 검토
    public record CountDto() {}
}
