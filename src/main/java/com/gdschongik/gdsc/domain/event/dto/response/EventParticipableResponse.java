package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.global.exception.ErrorCode;

public record EventParticipableResponse(boolean isParticipable, String errorCodeName) {

    public static EventParticipableResponse success() {
        return new EventParticipableResponse(true, null);
    }

    public static EventParticipableResponse failure(ErrorCode errorCode) {
        return new EventParticipableResponse(false, errorCode.name());
    }
}
