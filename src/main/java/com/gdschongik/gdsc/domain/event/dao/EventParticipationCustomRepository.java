package com.gdschongik.gdsc.domain.event.dao;

import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventParticipationCustomRepository {
    Page<EventApplicantResponse> findEventApplicants(
            Long eventId, EventParticipantQueryOption queryOption, Pageable pageable);
}
