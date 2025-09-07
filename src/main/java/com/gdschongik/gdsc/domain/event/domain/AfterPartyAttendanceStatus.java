package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

/**
 * 뒤풀이 참석 상태를 나타내는 Enum입니다.
 */
public enum AfterPartyAttendanceStatus {
    NONE, // 뒤풀이가 없는 경우
    NOT_ATTENDED,
    ATTENDED,
    ;

    public static AfterPartyAttendanceStatus getInitialStatus(Event event) {
        if (event.afterPartyExists()) {
            return NOT_ATTENDED;
        }
        return NONE;
    }

    public boolean isAttended() {
        return this == ATTENDED;
    }

    public boolean isNotAttended() {
        return this == NOT_ATTENDED;
    }

    public boolean isNone() {
        return this == NONE;
    }
}
