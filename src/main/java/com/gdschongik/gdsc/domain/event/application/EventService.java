package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.service.EventDomainService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventDto;
import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateBasicInfoRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateFormInfoRequest;
import com.gdschongik.gdsc.domain.event.dto.response.EventCreateResponse;
import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.lock.DistributedLock;
import java.time.LocalDateTime;
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
    public EventCreateResponse createEvent(EventCreateRequest request) {
        Event event = Event.create(
                request.name(),
                request.venue(),
                request.startAt(),
                request.description(),
                request.applicationPeriod(),
                request.regularRoleOnlyStatus(),
                request.mainEventMaxApplicantCount(),
                request.afterPartyMaxApplicantCount());
        eventRepository.save(event);

        log.info("[EventService] 이벤트 생성: eventId={}", event.getId());
        return EventCreateResponse.of(event.getId());
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvent(String name, Pageable pageable) {
        Page<Event> events = eventRepository.findAllByNameContains(name, pageable);

        List<EventResponse> response = events.stream()
                .map(event -> EventResponse.of(event, eventParticipationRepository.countByEvent(event)))
                .toList();

        return new PageImpl<>(response, pageable, events.getTotalElements());
    }

    @DistributedLock(key = "'event:' + #eventId")
    @Transactional
    public void updateEventBasicInfo(Long eventId, EventUpdateBasicInfoRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        long currentMainEventApplicantCount = eventParticipationRepository.countMainEventApplicantsByEvent(event);
        long currentAfterPartyApplicantCount = eventParticipationRepository.countAfterPartyApplicantsByEvent(event);

        eventDomainService.updateBasicInfo(
                event,
                request.name(),
                request.venue(),
                request.startAt(),
                request.description(),
                request.applicationPeriod(),
                request.regularRoleOnlyStatus(),
                request.mainEventMaxApplicantCount(),
                request.afterPartyMaxApplicantCount(),
                currentMainEventApplicantCount,
                currentAfterPartyApplicantCount);

        eventRepository.save(event);

        log.info("[EventService] 이벤트 기본 정보 수정 완료: eventId={}", event.getId());
    }

    @Transactional
    public void updateEventFormInfo(Long eventId, EventUpdateFormInfoRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        boolean eventParticipationExists = eventParticipationRepository.existsByEvent(event);

        eventDomainService.updateFormInfo(
                event,
                request.afterPartyStatus(),
                request.prePaymentStatus(),
                request.postPaymentStatus(),
                request.rsvpQuestionStatus(),
                request.noticeConfirmQuestionStatus(),
                eventParticipationExists);

        eventRepository.save(event);

        log.info("[EventService] 이벤트 폼 정보 수정 완료: eventId={}", event.getId());
    }

    @Transactional(readOnly = true)
    public EventDto getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        long currentMainEventApplicantCount = eventParticipationRepository.countMainEventApplicantsByEvent(event);

        eventDomainService.validateParticipantViewable(event, LocalDateTime.now(), currentMainEventApplicantCount);

        return EventDto.from(event);
    }
}
