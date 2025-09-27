package com.gdschongik.gdsc.domain.event.dto.response;

public record EventCreateResponse(Long eventId) {

    public static EventCreateResponse of(Long eventId) {
        return new EventCreateResponse(eventId);
    }
}
