package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.AssignmentHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.AssignmentHistory;
import com.gdschongik.gdsc.domain.study.domain.AssignmentSubmissionFetcher;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.domain.study.domain.StudySession;
import com.gdschongik.gdsc.domain.study.domain.service.AssignmentHistoryGrader;
import com.gdschongik.gdsc.domain.study.domain.service.AssignmentHistoryValidator;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import com.gdschongik.gdsc.infra.github.client.GithubClient;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAssignmentHistoryService {

    private final MemberUtil memberUtil;
    private final GithubClient githubClient;
    private final StudyRepository studyRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final AssignmentHistoryValidator assignmentHistoryValidator;
    private final AssignmentHistoryGrader assignmentHistoryGrader;

    @Transactional
    public void submitMyAssignment(Long studySessionId) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository
                .findFetchBySessionId(studySessionId)
                .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        Optional<StudyHistory> optionalStudyHistory =
                studyHistoryRepository.findByStudentAndStudy(currentMember, study);

        boolean isAppliedToStudy = optionalStudyHistory.isPresent();
        StudySession studySession = study.getStudySession(studySessionId);
        LocalDateTime now = LocalDateTime.now();

        assignmentHistoryValidator.validateSubmitAvailable(isAppliedToStudy, studySession, now);

        String repositoryLink =
                optionalStudyHistory.map(StudyHistory::getRepositoryLink).orElse(null);
        AssignmentSubmissionFetcher fetcher =
                githubClient.getLatestAssignmentSubmissionFetcher(repositoryLink, studySession.getPosition());
        AssignmentHistory assignmentHistory = findOrCreate(currentMember, studySession);

        assignmentHistoryGrader.judge(fetcher, assignmentHistory, study.getMinAssignmentLength());

        assignmentHistoryRepository.save(assignmentHistory);

        log.info(
                "[StudentAssignmentHistoryService] 과제 제출: studySessionId={}, studentId={}, submissionStatus={}, submissionFailureType={}",
                studySessionId,
                currentMember.getId(),
                assignmentHistory.getSubmissionStatus(),
                assignmentHistory.getSubmissionFailureType());
    }

    private AssignmentHistory findOrCreate(Member student, StudySession studySession) {
        return assignmentHistoryRepository
                .findByMemberAndStudySession(student, studySession)
                .orElseGet(() -> {
                    AssignmentHistory assignmentHistory = AssignmentHistory.create(studySession, student);
                    return assignmentHistoryRepository.save(assignmentHistory);
                });
    }
}
