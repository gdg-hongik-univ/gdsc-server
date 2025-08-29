package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.dto.dto.EventDto;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record EventResponse(EventDto eventDto, Long totalAttendeesCount, EventStatusType eventStatusType) {

    public static EventResponse of(Event event, Long totalAttendeesCount) {
        EventDto eventDto = EventDto.from(event);
        EventStatusType eventStatusType =
                EventStatusType.of(event.getApplicationPeriod(), event.getStartAt(), LocalDateTime.now());
        return new EventResponse(eventDto, totalAttendeesCount, eventStatusType);
    }

    @Getter
    @RequiredArgsConstructor
    public enum EventStatusType {
        BEFORE_APPLICATION("신청 전"),
        APPLICATION_OPEN("신청 중"),
        APPLICATION_CLOSED("신청 종료"),
        EVENT_ENDED("행사 종료");

        private final String value;

        private static EventStatusType of(Period applicationPeriod, LocalDateTime startAt, LocalDateTime now) {
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
