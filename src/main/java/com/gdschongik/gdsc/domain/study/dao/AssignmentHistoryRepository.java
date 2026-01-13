package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.AssignmentHistory;
import com.gdschongik.gdsc.domain.study.domain.AssignmentSubmissionStatus;
import com.gdschongik.gdsc.domain.study.domain.StudySession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentHistoryRepository
        extends JpaRepository<AssignmentHistory, Long>, AssignmentHistoryCustomRepository {
    Optional<AssignmentHistory> findByMemberAndStudySession(Member member, StudySession studySession);

    long countByStudySessionIdAndSubmissionStatusEquals(Long studySessionId, AssignmentSubmissionStatus status);
}
