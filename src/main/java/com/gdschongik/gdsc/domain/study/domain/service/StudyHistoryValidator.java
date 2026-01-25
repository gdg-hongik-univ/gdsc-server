package com.gdschongik.gdsc.domain.study.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@DomainService
public class StudyHistoryValidator {

    public void validateAppliedToStudy(long countStudyHistory, int studentCount) {
        if (countStudyHistory != studentCount) {
            throw new CustomException(STUDY_HISTORY_NOT_APPLIED_STUDENT_EXISTS);
        }
    }

    public void validateUpdateRepository(String repositoryOwnerGithubId, Member currentMember) {
        // 레포지토리 소유자가 현 멤버가 아닌 경우
        String currentMemberGithubId = currentMember.getOauthId();

        if (!Objects.equals(repositoryOwnerGithubId, currentMemberGithubId)) {
            throw new CustomException(STUDY_HISTORY_REPOSITORY_NOT_UPDATABLE_OWNER_MISMATCH);
        }
    }

    public void validateApplyStudy(Study study, List<StudyHistory> currentMemberStudyHistories, LocalDateTime now) {
        // 스터디 수강 신청 기간이 아닌 경우
        if (!study.isApplicable(now)) {
            throw new CustomException(STUDY_NOT_APPLICABLE);
        }

        // 이미 해당 스터디에 수강 신청한 경우
        boolean isStudyHistoryDuplicate = currentMemberStudyHistories.stream()
                .anyMatch(studyHistory -> studyHistory.getStudy().getId() == study.getId());

        if (isStudyHistoryDuplicate) {
            throw new CustomException(STUDY_HISTORY_DUPLICATE);
        }
    }

    public void validateCancelStudyApply(Study study, LocalDateTime now) {
        // 스터디 수강신청 기간이 아닌 경우
        if (!study.isApplicable(now)) {
            throw new CustomException(STUDY_NOT_CANCELABLE_APPLICATION_PERIOD);
        }
    }
}
