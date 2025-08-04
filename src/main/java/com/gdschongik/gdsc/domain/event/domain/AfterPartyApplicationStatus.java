package com.gdschongik.gdsc.domain.event.domain;

/**
 * 뒤풀이 신청 상태를 나타내는 Enum입니다.
 * 사용자 요청에 따라 값이 변경되므로 getInitialStatus() 메서드는 필요하지 않습니다.
 */
public enum AfterPartyApplicationStatus {
    NONE, // 뒤풀이가 없는 경우
    NOT_APPLIED,
    APPLIED,
    ;

    public boolean isNone() {
        return this == NONE;
    }
}
