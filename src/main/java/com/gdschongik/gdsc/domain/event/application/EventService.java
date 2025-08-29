package com.gdschongik.gdsc.domain.event.application;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipationRepository eventParticipationRepository;

    @Transactional(readOnly = true)
    public Page<EventResponse> getEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);

        List<EventResponse> response = new ArrayList<>();
        events.forEach(event -> {
            long countByEvent = eventParticipationRepository.countByEvent(event);
            response.add(EventResponse.of(event, countByEvent));
        });

        return new PageImpl<>(response, pageable, events.getTotalElements());
    }
}
