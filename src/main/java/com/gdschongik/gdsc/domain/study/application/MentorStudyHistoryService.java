package com.gdschongik.gdsc.domain.study.application;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.domain.study.domain.event.StudyHistoriesCompletedEvent;
import com.gdschongik.gdsc.domain.study.domain.service.StudyHistoryValidator;
import com.gdschongik.gdsc.domain.study.domain.service.StudyValidator;
import com.gdschongik.gdsc.domain.study.dto.request.StudyCompleteRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorStudyHistoryService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberUtil memberUtil;
    private final StudyValidator studyValidator;
    private final StudyHistoryValidator studyHistoryValidator;
    private final StudyRepository studyRepository;
    private final StudyHistoryRepository studyHistoryRepository;

    @Transactional
    public void completeStudy(StudyCompleteRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository
                .findById(request.studyId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

        studyValidator.validateStudyMentor(currentMember, study);

        List<StudyHistory> studyHistories =
                studyHistoryRepository.findAllByStudyIdAndStudentIds(request.studyId(), request.studentIds());

        studyHistoryValidator.validateAppliedToStudy(
                studyHistories.size(), request.studentIds().size());

        studyHistories.forEach(StudyHistory::complete);

        applicationEventPublisher.publishEvent(new StudyHistoriesCompletedEvent(
                studyHistories.stream().map(StudyHistory::getId).toList()));

        log.info(
                "[MentorStudyHistoryService] 스터디 수료 처리: studyId={}, studentIds={}",
                request.studyId(),
                request.studentIds());
    }

    @Transactional
    public void withdrawStudyCompletion(StudyCompleteRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository
                .findById(request.studyId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

        studyValidator.validateStudyMentor(currentMember, study);

        List<StudyHistory> studyHistories =
                studyHistoryRepository.findAllByStudyIdAndStudentIds(request.studyId(), request.studentIds());

        studyHistoryValidator.validateAppliedToStudy(
                studyHistories.size(), request.studentIds().size());

        studyHistories.forEach(StudyHistory::withdrawCompletion);

        studyHistoryRepository.saveAll(studyHistories);

        log.info(
                "[MentorStudyHistoryService] 스터디 수료 철회: studyId={}, studentIds={}",
                request.studyId(),
                request.studentIds());
    }
}
