package com.gdschongik.gdsc.domain.event.application;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.dto.dto.EventDto;
import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
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
}
