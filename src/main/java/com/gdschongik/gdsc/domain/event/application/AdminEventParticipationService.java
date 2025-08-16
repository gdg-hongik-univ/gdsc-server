package com.gdschongik.gdsc.domain.event.application;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipationDto;
import com.gdschongik.gdsc.domain.event.dto.response.EventParticipationAfterPartyResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEventParticipationService {

    private final EventParticipationRepository eventParticipationRepository;

    @Transactional(readOnly = true)
    public EventParticipationAfterPartyResponse getAfterPartyAttendance(Long eventId) {
        List<EventParticipation> eventParticipations = eventParticipationRepository.findAllByEventId(eventId);

        long attendedAfterApplyingCount = eventParticipations.stream()
                .filter(eventParticipation -> eventParticipation
                                .getAfterPartyApplicationStatus()
                                .isApplied()
                        && eventParticipation.getAfterPartyAttendanceStatus().isAttended())
                .count();

        long notAttendedAfterApplyingCount = eventParticipations.stream()
                .filter(eventParticipation -> eventParticipation
                                .getAfterPartyApplicationStatus()
                                .isApplied()
                        && eventParticipation.getAfterPartyAttendanceStatus().isNotAttended())
                .count();

        long onSiteApplicationCount = eventParticipations.stream()
                .filter(eventParticipation -> eventParticipation
                                .getAfterPartyApplicationStatus()
                                .isNotApplied()
                        && eventParticipation.getAfterPartyAttendanceStatus().isAttended())
                .count();

        List<EventParticipationDto> eventParticipationDtos =
                eventParticipations.stream().map(EventParticipationDto::from).toList();

        return EventParticipationAfterPartyResponse.of(
                attendedAfterApplyingCount,
                notAttendedAfterApplyingCount,
                onSiteApplicationCount,
                eventParticipationDtos);
    }
}
