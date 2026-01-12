package com.gdschongik.gdsc.domain.study.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class StudyAchievementValidator {

    public void validateDesignateOutstandingStudent(long studyAchievementsAlreadyExistCount) {
        // 이미 우수 스터디원으로 지정된 스터디원이 있는 경우
        if (studyAchievementsAlreadyExistCount > 0) {
            throw new CustomException(STUDY_ACHIEVEMENT_ALREADY_EXISTS);
        }
    }
}
