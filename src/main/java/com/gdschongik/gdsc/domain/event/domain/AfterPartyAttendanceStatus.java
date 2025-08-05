package com.gdschongik.gdsc.domain.event.domain;

public enum AfterPartyAttendanceStatus {
    NONE, // 뒤풀이가 없는 경우
    NOT_ATTENDED,
    ATTENDED,
    ;

    public static AfterPartyAttendanceStatus getInitialStatus(Event event) {
        if (event.getAfterPartyStatus().isEnabled()) {
            return NOT_ATTENDED;
        }
        return NONE;
    }
}
