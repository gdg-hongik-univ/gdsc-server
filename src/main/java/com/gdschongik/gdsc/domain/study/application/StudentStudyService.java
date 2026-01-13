package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.recruitment.dao.RecruitmentRepository;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.study.dao.AssignmentHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.AttendanceRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.*;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySimpleDto;
import com.gdschongik.gdsc.domain.study.dto.response.StudyApplicableResponse;
import com.gdschongik.gdsc.domain.study.dto.response.StudyDashboardResponse;
import com.gdschongik.gdsc.domain.study.dto.response.StudyTodoResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentStudyService {

    private final MemberUtil memberUtil;
    private final StudyRepository studyRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final RecruitmentRepository recruitmentRepository;

    @Transactional(readOnly = true)
    public StudyDashboardResponse getMyStudyDashboard(Long studyId) {
        Member member = memberUtil.getCurrentMember();
        Study study = studyRepository.findFetchById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        StudyHistory studyHistory = studyHistoryRepository
                .findByStudentAndStudy(member, study)
                .orElseThrow(() -> new CustomException(STUDY_HISTORY_NOT_FOUND));
        List<Attendance> attendances = attendanceRepository.findFetchByMemberAndStudy(member, study);
        List<AssignmentHistory> assignmentHistories = assignmentHistoryRepository.findByMemberAndStudy(member, study);
        LocalDateTime now = LocalDateTime.now();

        return StudyDashboardResponse.of(study, studyHistory, attendances, assignmentHistories, now);
    }

    @Transactional(readOnly = true)
    public StudyApplicableResponse getAllApplicableStudies() {
        Member currentMember = memberUtil.getCurrentMember();
        LocalDateTime now = LocalDateTime.now();

        List<StudyHistory> studyHistories = studyHistoryRepository.findAllByStudent(currentMember).stream()
                .filter(studyHistory -> studyHistory.getStudy().isApplicable(now))
                .toList();

        List<Study> applicableStudies = studyRepository.findAll().stream()
                .filter(study -> study.isApplicable(now))
                .toList();

        return StudyApplicableResponse.of(studyHistories, applicableStudies);
    }

    @Transactional(readOnly = true)
    public List<StudySimpleDto> getMyCurrentStudies() {
        Member currentMember = memberUtil.getCurrentMember();
        LocalDateTime now = LocalDateTime.now();

        Recruitment recruitment = recruitmentRepository
                .findCurrentRecruitment(now)
                .orElseThrow(() -> new CustomException(RECRUITMENT_NOT_FOUND));

        return studyHistoryRepository.findAllByStudent(currentMember).stream()
                .filter(studyHistory -> studyHistory.getStudy().getSemester().equals(recruitment.getSemester()))
                .map(studyHistory -> StudySimpleDto.from(studyHistory.getStudy()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudyTodoResponse> getMyStudyTodos(Long studyId) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository.findFetchById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        StudyHistory studyHistory = studyHistoryRepository
                .findByStudentAndStudy(currentMember, study)
                .orElseThrow(() -> new CustomException(STUDY_HISTORY_NOT_FOUND));
        List<Attendance> attendances = attendanceRepository.findFetchByMemberAndStudy(currentMember, study);
        List<AssignmentHistory> assignmentHistories =
                assignmentHistoryRepository.findByMemberAndStudy(currentMember, study);

        LocalDateTime now = LocalDateTime.now();
        List<StudyTodoResponse> response = new ArrayList<>();

        response.addAll(getAttendanceTodos(study, attendances, now));
        response.addAll(getAssignmentTodos(study, studyHistory, assignmentHistories, now));

        return response;
    }

    @Transactional(readOnly = true)
    public List<StudyTodoResponse> getMyStudiesTodos() {
        Member currentMember = memberUtil.getCurrentMember();
        LocalDateTime now = LocalDateTime.now();

        Recruitment recruitment = recruitmentRepository
                .findCurrentRecruitment(now)
                .orElseThrow(() -> new CustomException(RECRUITMENT_NOT_FOUND));

        List<Study> currentStudies = studyHistoryRepository.findAllByStudent(currentMember).stream()
                .map(StudyHistory::getStudy)
                .filter(study -> study.getSemester().equals(recruitment.getSemester()))
                .toList();

        List<StudyTodoResponse> response = new ArrayList<>();

        currentStudies.forEach(study -> {
            StudyHistory studyHistory = studyHistoryRepository
                    .findByStudentAndStudy(currentMember, study)
                    .orElseThrow(() -> new CustomException(STUDY_HISTORY_NOT_FOUND));
            List<Attendance> attendances = attendanceRepository.findFetchByMemberAndStudy(currentMember, study);
            List<AssignmentHistory> assignmentHistories =
                    assignmentHistoryRepository.findByMemberAndStudy(currentMember, study);

            response.addAll(getAttendanceTodos(study, attendances, now));
            response.addAll(getAssignmentTodos(study, studyHistory, assignmentHistories, now));
        });
        return response;
    }

    private List<StudyTodoResponse> getAttendanceTodos(Study study, List<Attendance> attendances, LocalDateTime now) {
        return study.getStudySessions().stream()
                .filter(studySession -> studySession.isAttendable(now))
                .map(studySession -> StudyTodoResponse.attendanceType(study, studySession, attendances, now))
                .toList();
    }

    private List<StudyTodoResponse> getAssignmentTodos(
            Study study, StudyHistory studyHistory, List<AssignmentHistory> assignmentHistories, LocalDateTime now) {
        return study.getStudySessions().stream()
                .filter(studySession -> studySession.isAssignmentSubmittable(now))
                .map(studySession ->
                        StudyTodoResponse.assignmentType(study, studyHistory, studySession, assignmentHistories, now))
                .toList();
    }
}
