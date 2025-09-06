package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.service.EventDomainService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventDto;
import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateRequest;
import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipationRepository eventParticipationRepository;
    private final EventDomainService eventDomainService;

    @Transactional(readOnly = true)
    public Page<EventResponse> getEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);

        List<EventResponse> response = events.stream()
                .map(event -> EventResponse.of(event, eventParticipationRepository.countByEvent(event)))
                .toList();

        return new PageImpl<>(response, pageable, events.getTotalElements());
    }

    @Transactional
    public void createEvent(EventCreateRequest request) {
        Event event = Event.create(
                request.name(),
                request.venue(),
                request.startAt(),
                request.applicationDescription(),
                request.applicationPeriod(),
                request.regularRoleOnlyStatus(),
                request.afterPartyStatus(),
                request.prePaymentStatus(),
                request.postPaymentStatus(),
                request.rsvpQuestionStatus(),
                request.mainEventMaxApplicantCount(),
                request.afterPartyMaxApplicantCount());
        eventRepository.save(event);

        log.info("[EventService] 이벤트 생성: eventId={}", event.getId());
    }

    @Transactional(readOnly = true)
    public List<EventDto> searchEvent(String name) {
        List<Event> events = eventRepository.findAllByNameContains(name);
        return events.stream().map(EventDto::from).toList();
    }

    @Transactional
    public void updateEvent(Long eventId, EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

        int currentMainEventApplicants = eventParticipationRepository.countMainEventApplicantsByEvent(event);
        eventDomainService.validateWhenUpdateMaxApplicantCount(
                currentMainEventApplicants, request.mainEventMaxApplicantCount());

        int currentAfterPartyApplicants = eventParticipationRepository.countAfterPartyApplicantsByEvent(event);
        eventDomainService.validateWhenUpdateMaxApplicantCount(
                currentAfterPartyApplicants, request.afterPartyMaxApplicantCount());

        event.update(
                request.name(),
                request.venue(),
                request.startAt(),
                request.applicationDescription(),
                request.applicationPeriod(),
                request.mainEventMaxApplicantCount(),
                request.afterPartyMaxApplicantCount());

        eventRepository.save(event);

        log.info("[EventService] 이벤트 수정 완료: eventId={}", event.getId());
    }
}
