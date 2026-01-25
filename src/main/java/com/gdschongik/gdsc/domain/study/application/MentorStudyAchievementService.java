package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.StudyAchievementRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyAchievement;
import com.gdschongik.gdsc.domain.study.domain.service.StudyAchievementValidator;
import com.gdschongik.gdsc.domain.study.domain.service.StudyHistoryValidator;
import com.gdschongik.gdsc.domain.study.domain.service.StudyValidator;
import com.gdschongik.gdsc.domain.study.dto.request.OutstandingStudentRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorStudyAchievementService {

    private final MemberUtil memberUtil;
    private final StudyValidator studyValidator;
    private final StudyHistoryValidator studyHistoryValidator;
    private final StudyAchievementValidator studyAchievementValidator;
    private final StudyRepository studyRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final StudyAchievementRepository studyAchievementRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void designateOutstandingStudent(Long studyId, OutstandingStudentRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        studyValidator.validateStudyMentor(currentMember, study);

        long studyHistoryCount = studyHistoryRepository.countByStudyIdAndStudentIds(studyId, request.studentIds());
        studyHistoryValidator.validateAppliedToStudy(
                studyHistoryCount, request.studentIds().size());

        long studyAchievementsAlreadyExistCount =
                studyAchievementRepository.countByStudyIdAndAchievementTypeAndStudentIds(
                        studyId, request.achievementType(), request.studentIds());
        studyAchievementValidator.validateDesignateOutstandingStudent(studyAchievementsAlreadyExistCount);

        List<Member> outstandingStudents = memberRepository.findAllById(request.studentIds());
        List<StudyAchievement> studyAchievements = outstandingStudents.stream()
                .map(member -> StudyAchievement.create(request.achievementType(), member, study))
                .toList();
        studyAchievementRepository.saveAll(studyAchievements);

        log.info(
                "[MentorStudyAchievementService] 우수 스터디원 지정: studyId={}, studentIds={}", studyId, request.studentIds());
    }

    @Transactional
    public void withdrawOutstandingStudent(Long studyId, OutstandingStudentRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        studyValidator.validateStudyMentor(currentMember, study);

        long studyHistoryCount = studyHistoryRepository.countByStudyIdAndStudentIds(studyId, request.studentIds());
        studyHistoryValidator.validateAppliedToStudy(
                studyHistoryCount, request.studentIds().size());

        studyAchievementRepository.deleteByStudyAndAchievementTypeAndMemberIds(
                studyId, request.achievementType(), request.studentIds());

        log.info(
                "[MentorStudyAchievementService] 우수 스터디원 철회: studyId={}, studentIds={}", studyId, request.studentIds());
    }
}
