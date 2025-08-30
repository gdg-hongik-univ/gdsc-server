package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.dto.dto.EventDto;
import java.time.LocalDateTime;

public record EventResponse(EventDto event, Long totalAttendeesCount, EventStatus eventStatus) {

    public static EventResponse of(Event event, Long totalAttendeesCount) {
        EventDto eventDto = EventDto.from(event);
        EventStatus eventStatus = EventStatus.of(event.getApplicationPeriod(), event.getStartAt(), LocalDateTime.now());
        return new EventResponse(eventDto, totalAttendeesCount, eventStatus);
    }

    public enum EventStatus {
        BEFORE_APPLICATION,
        APPLICATION_OPEN,
        APPLICATION_CLOSED,
        EVENT_ENDED;

        private static EventStatus of(Period applicationPeriod, LocalDateTime startAt, LocalDateTime now) {
            if (now.isBefore(applicationPeriod.getStartDate())) {
                return BEFORE_APPLICATION;
            } else if (applicationPeriod.isWithin(now)) {
                return APPLICATION_OPEN;
            } else if (now.isAfter(startAt)) {
                return EVENT_ENDED;
            } else {
                return APPLICATION_CLOSED;
            }
        }
    }
}
