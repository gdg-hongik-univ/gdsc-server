package com.gdschongik.gdsc.domain.study.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.ATTENDANCE_NUMBER_MISMATCH;
import static com.gdschongik.gdsc.global.exception.ErrorCode.STUDY_HISTORY_NOT_FOUND;
import static com.gdschongik.gdsc.global.exception.ErrorCode.STUDY_SESSION_ALREADY_ATTENDED;
import static com.gdschongik.gdsc.global.exception.ErrorCode.STUDY_SESSION_NOT_ATTENDABLE_PERIOD_MISMATCH;

import com.gdschongik.gdsc.domain.study.domain.StudySession;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;

@DomainService
public class AttendanceValidator {

    public void validateAttendance(
            StudySession studySession,
            String attendanceNumber,
            boolean isAppliedToStudy,
            boolean isAlreadyAttended,
            LocalDateTime now) {
        // 스터디 신청 여부 검증
        if (!isAppliedToStudy) {
            throw new CustomException(STUDY_HISTORY_NOT_FOUND);
        }

        // 스터디 중복 출석체크 여부 검증
        if (isAlreadyAttended) {
            throw new CustomException(STUDY_SESSION_ALREADY_ATTENDED);
        }

        // 출석체크 가능 기간 검증
        if (!studySession.getLessonPeriod().isWithin(now)) {
            throw new CustomException(STUDY_SESSION_NOT_ATTENDABLE_PERIOD_MISMATCH);
        }

        // 출석체크 번호 검증
        if (!studySession.getLessonAttendanceNumber().equals(attendanceNumber)) {
            throw new CustomException(ATTENDANCE_NUMBER_MISMATCH);
        }
    }
}
