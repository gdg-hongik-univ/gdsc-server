package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.global.exception.ErrorCode;

public record EventValidateApplicableResponse(boolean isParticipable, String errorCodeName) {

    public static EventValidateApplicableResponse success() {
        return new EventValidateApplicableResponse(true, null);
    }

    public static EventValidateApplicableResponse failure(ErrorCode errorCode) {
        return new EventValidateApplicableResponse(false, errorCode.name());
    }
}
