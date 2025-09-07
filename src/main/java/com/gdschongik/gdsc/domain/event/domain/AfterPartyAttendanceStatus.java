package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.exception.CustomException;

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

    public AfterPartyAttendanceStatus confirm() {
        if (isNone()) throw new CustomException(AFTER_PARTY_ATTENDANCE_STATUS_NOT_UPDATABLE_NONE);
        if (isAttended()) throw new CustomException(AFTER_PARTY_ATTENDANCE_STATUS_ALREADY_UPDATED);
        return ATTENDED;
    }

    public AfterPartyAttendanceStatus revoke() {
        if (isNone()) throw new CustomException(AFTER_PARTY_ATTENDANCE_STATUS_NOT_UPDATABLE_NONE);
        if (isNotAttended()) throw new CustomException(AFTER_PARTY_ATTENDANCE_STATUS_ALREADY_UPDATED);
        return NOT_ATTENDED;
    }
}
