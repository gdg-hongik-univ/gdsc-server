package com.gdschongik.gdsc.domain.study.domain;

import com.gdschongik.gdsc.domain.common.vo.Period;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendanceStatus {
    NOT_LIVE("미실시"),
    BEFORE_ATTENDANCE("출석 전"),
    NOT_ATTENDED("미출석"),
    ATTENDED("출석"),
    ;

    private final String value;

    public static AttendanceStatus of(
            StudySession studySession, StudyType type, boolean isAttended, LocalDateTime now) {
        if (!type.isLive()) {
            return NOT_LIVE;
        }

        Period lessonPeriod = studySession.getLessonPeriod();

        if (lessonPeriod == null || lessonPeriod.getStartDate().isAfter(now)) {
            return BEFORE_ATTENDANCE;
        }

        if (isAttended) {
            return ATTENDED;
        } else {
            return NOT_ATTENDED;
        }
    }
}
