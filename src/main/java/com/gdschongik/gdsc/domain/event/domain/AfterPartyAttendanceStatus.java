package com.gdschongik.gdsc.domain.event.domain;

public enum AfterPartyAttendanceStatus {
    NONE, // 뒤풀이가 없는 경우
    NOT_ATTENDED,
    ATTENDED,
    ;

    /**
     * 뒤풀이 참석 상태의 초기값을 반환합니다.
     * 뒤풀이가 비활성화된 이벤트의 경우 초기값은 NONE 이고 활성화된 경우 초기값은 NOT_ATTENDED 입니다.
     */
    public static AfterPartyAttendanceStatus getInitialStatus(Event event) {
        if (event.getAfterPartyStatus() == UsageStatus.DISABLED) {
            return NONE;
        }
        return NOT_ATTENDED;
    }
}
