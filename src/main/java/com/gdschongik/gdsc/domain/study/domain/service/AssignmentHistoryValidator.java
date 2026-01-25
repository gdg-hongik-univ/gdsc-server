package com.gdschongik.gdsc.domain.study.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.study.domain.StudySession;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;

@DomainService
public class AssignmentHistoryValidator {

    /**
     * 채점을 수행하기 전, 과제 제출이 가능한지 검증합니다.
     */
    public void validateSubmitAvailable(boolean isAppliedToStudy, StudySession studySession, LocalDateTime now) {
        if (!isAppliedToStudy) {
            throw new CustomException(ASSIGNMENT_STUDY_NOT_APPLIED);
        }

        studySession.validateAssignmentSubmittable(now);
    }
}
