package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static java.util.stream.Collectors.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.StudyAchievementRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyAchievement;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.domain.study.domain.service.StudyHistoryValidator;
import com.gdschongik.gdsc.domain.study.dto.request.StudyApplyCancelRequest;
import com.gdschongik.gdsc.domain.study.dto.request.StudyApplyRequest;
import com.gdschongik.gdsc.domain.study.dto.request.StudyHistoryRepositoryUpdateRequest;
import com.gdschongik.gdsc.domain.study.dto.response.StudyHistoryMyResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import com.gdschongik.gdsc.infra.github.client.GithubClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentStudyHistoryService {

    private final GithubClient githubClient;
    private final MemberUtil memberUtil;
    private final StudyRepository studyRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final StudyAchievementRepository studyAchievementRepository;
    private final StudyHistoryValidator studyHistoryValidator;

    @Transactional
    public void updateMyRepository(StudyHistoryRepositoryUpdateRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study =
                studyRepository.findById(request.studyId()).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        StudyHistory studyHistory = studyHistoryRepository
                .findByStudentAndStudy(currentMember, study)
                .orElseThrow(() -> new CustomException(STUDY_HISTORY_NOT_FOUND));
        String repositoryOwnerId = githubClient.getOwnerId(request.repositoryLink());

        studyHistoryValidator.validateUpdateRepository(repositoryOwnerId, currentMember);

        studyHistory.updateRepositoryLink(request.repositoryLink());
        studyHistoryRepository.save(studyHistory);

        log.info(
                "[StudentStudyHistoryService] 내 레포지토리 입력 완료: studyHistoryId={}, repositoryLink={}",
                studyHistory.getId(),
                request.repositoryLink());
    }

    @Transactional(readOnly = true)
    public List<StudyHistoryMyResponse> getMyStudyHistories() {
        Member currentMember = memberUtil.getCurrentMember();
        List<StudyHistory> studyHistories = studyHistoryRepository.findAllByStudent(currentMember);
        List<StudyAchievement> studyAchievements = studyAchievementRepository.findAllByStudent(currentMember);

        Map<Study, List<StudyAchievement>> achievementsByStudy =
                studyAchievements.stream().collect(groupingBy(StudyAchievement::getStudy));

        return studyHistories.stream()
                .map(history -> StudyHistoryMyResponse.of(
                        history, history.getStudy(), achievementsByStudy.getOrDefault(history.getStudy(), List.of())))
                .toList();
    }

    @Transactional
    public void applyStudy(StudyApplyRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study =
                studyRepository.findById(request.studyId()).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        List<StudyHistory> studyHistories = studyHistoryRepository.findAllByStudent(currentMember);
        LocalDateTime now = LocalDateTime.now();

        studyHistoryValidator.validateApplyStudy(study, studyHistories, now);

        StudyHistory studyHistory = StudyHistory.create(currentMember, study);
        studyHistoryRepository.save(studyHistory);

        log.info("[StudentStudyHistoryService] 스터디 수강신청: studyHistoryId={}", studyHistory.getId());
    }

    @Transactional
    public void cancelStudyApply(StudyApplyCancelRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study =
                studyRepository.findById(request.studyId()).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();

        studyHistoryValidator.validateCancelStudyApply(study, now);

        StudyHistory studyHistory = studyHistoryRepository
                .findByStudentAndStudy(currentMember, study)
                .orElseThrow(() -> new CustomException(STUDY_HISTORY_NOT_FOUND));
        studyHistoryRepository.delete(studyHistory);

        log.info(
                "[StudentStudyHistoryService] 스터디 수강신청 취소: appliedStudyId={}, memberId={}",
                study.getId(),
                currentMember.getId());
    }
}
