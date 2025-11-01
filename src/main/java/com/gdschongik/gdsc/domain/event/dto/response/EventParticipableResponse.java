package com.gdschongik.gdsc.domain.event.dto.response;

public record EventParticipableResponse(boolean isParticipable, String failureReason) {

    public static EventParticipableResponse success() {
        return new EventParticipableResponse(true, null);
    }

    public static EventParticipableResponse failure(String failureReason) {
        return new EventParticipableResponse(false, failureReason);
    }
}
