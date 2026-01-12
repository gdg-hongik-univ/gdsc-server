package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.*;
import com.gdschongik.gdsc.domain.study.domain.AttendanceNumberGenerator;
import com.gdschongik.gdsc.domain.study.domain.StudyFactory;
import com.gdschongik.gdsc.domain.study.dto.request.StudyCreateRequest;
import com.gdschongik.gdsc.domain.study.dto.response.StudyManagerResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final StudyFactory studyFactory;
    private final AttendanceNumberGenerator attendanceNumberGenerator;

    @Transactional
    public void createStudy(StudyCreateRequest request) {
        Member mentor =
                memberRepository.findById(request.mentorId()).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        mentor.assignToMentor();

        Study study = studyFactory.create(
                request.type(),
                request.title(),
                request.semester(),
                request.totalRound(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.applicationPeriod(),
                request.discordChannelId(),
                request.discordRoleId(),
                mentor,
                attendanceNumberGenerator,
                request.minAssignmentLength());

        memberRepository.save(mentor);
        studyRepository.save(study);

        log.info("[AdminStudyService] 스터디 생성 완료: studyId = {}", study.getId());
    }

    @Transactional(readOnly = true)
    public List<StudyManagerResponse> getAllStudies() {
        return studyRepository.findFetchAll().stream()
                .map(StudyManagerResponse::from)
                .toList();
    }

    public void deleteStudy(Long studyId) {
        try {
            studyRepository.deleteById(studyId);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(STUDY_NOT_DELETABLE_FK_CONSTRAINT);
        }

        log.info("[AdminStudyService] 스터디 삭제 완료: studyId = {}", studyId);
    }
}
