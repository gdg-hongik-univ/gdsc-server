package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.domain.study.domain.AssignmentSubmissionStatus.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static java.util.stream.Collectors.groupingBy;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.*;
import com.gdschongik.gdsc.domain.study.domain.*;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import com.gdschongik.gdsc.domain.study.domain.StudyUpdateCommand;
import com.gdschongik.gdsc.domain.study.domain.service.StudyValidator;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyRoundStatisticsDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyTaskDto;
import com.gdschongik.gdsc.domain.study.dto.request.StudyUpdateRequest;
import com.gdschongik.gdsc.domain.study.dto.response.*;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.ExcelUtil;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorStudyService {

    private final MemberUtil memberUtil;
    private final ExcelUtil excelUtil;
    private final StudyValidator studyValidator;
    private final StudyRepository studyRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final StudyAchievementRepository studyAchievementRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;

    @Transactional(readOnly = true)
    public List<StudyManagerResponse> getStudiesInCharge() {
        Member mentor = memberUtil.getCurrentMember();
        List<Study> myStudies = studyRepository.findAllByMentor(mentor);
        return myStudies.stream().map(StudyManagerResponse::from).toList();
    }

    @Transactional
    public void updateStudy(Long studyId, StudyUpdateRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository.findFetchById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        studyValidator.validateStudyMentor(currentMember, study);

        StudyUpdateCommand command = request.toCommand();

        study.update(command);
        studyRepository.save(study);

        log.info("[MentorStudyService] 스터디 정보 수정 완료: studyId={}", studyId);
    }

    @Transactional(readOnly = true)
    public StudyStatisticsResponse getStudyStatistics(Long studyId) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        studyValidator.validateStudyMentor(currentMember, study);

        List<StudyHistory> studyHistories = studyHistoryRepository.findAllByStudy(study);
        List<StudySession> studySessions = study.getStudySessions();

        long totalStudentCount = studyHistories.size();
        long studyCompletedStudentCount =
                studyHistories.stream().filter(StudyHistory::isCompleted).count();

        List<StudyRoundStatisticsDto> studyRoundStatisticsDtos = studySessions.stream()
                .map(studySession -> calculateRoundStatistics(studySession, totalStudentCount))
                .toList();

        long averageAttendanceRate = calculateAverageWeekAttendanceRate(studyRoundStatisticsDtos);
        long averageAssignmentSubmissionRate = calculateAverageWeekAssignmentSubmissionRate(studyRoundStatisticsDtos);

        return StudyStatisticsResponse.of(
                totalStudentCount,
                studyCompletedStudentCount,
                averageAttendanceRate,
                averageAssignmentSubmissionRate,
                studyRoundStatisticsDtos);
    }

    @Transactional
    public Page<MentorStudyStudentResponse> getStudyStudents(Long studyId, Pageable pageable) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        studyValidator.validateStudyMentor(currentMember, study);

        LocalDateTime now = LocalDateTime.now();
        StudyType type = study.getType();
        Page<StudyHistory> studyHistories = studyHistoryRepository.findByStudyId(studyId, pageable);
        List<Long> studentIds = studyHistories.stream()
                .map(studyHistory -> studyHistory.getStudent().getId())
                .toList();
        List<StudySession> studySessions = study.getStudySessions();

        Map<Long, List<StudyAchievement>> studyAchievementMap = getStudyAchievementMap(studyId, studentIds);
        Map<Long, List<Attendance>> attendanceMap = getAttendanceMap(studyId, studentIds);
        Map<Long, List<AssignmentHistory>> assignmentHistoryMap = getAssignmentHistoryMap(studyId, studentIds);

        List<MentorStudyStudentResponse> response = new ArrayList<>();

        studyHistories.forEach(studyHistory -> {
            List<StudyAchievement> currentStudyAchievements =
                    studyAchievementMap.getOrDefault(studyHistory.getStudent().getId(), List.of());
            List<Attendance> currentAttendances =
                    attendanceMap.getOrDefault(studyHistory.getStudent().getId(), List.of());
            List<AssignmentHistory> currentAssignmentHistories =
                    assignmentHistoryMap.getOrDefault(studyHistory.getStudent().getId(), List.of());

            List<StudyTaskDto> studyTasks = new ArrayList<>();
            studySessions.forEach(studySession -> {
                studyTasks.add(StudyTaskDto.of(studySession, type, isAttended(currentAttendances, studySession), now));
                studyTasks.add(StudyTaskDto.of(
                        studySession, getSubmittedAssignment(currentAssignmentHistories, studySession), now));
            });

            response.add(MentorStudyStudentResponse.of(studyHistory, currentStudyAchievements, studyTasks));
        });
        return new PageImpl<>(response, pageable, studyHistories.getTotalElements());
    }

    @Transactional
    public byte[] createStudyExcel(Long studyId) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        studyValidator.validateStudyMentor(currentMember, study);

        LocalDateTime now = LocalDateTime.now();
        StudyType type = study.getType();
        List<StudyHistory> studyHistories = studyHistoryRepository.findAllByStudy(study);
        List<Long> studentIds = studyHistories.stream()
                .map(studyHistory -> studyHistory.getStudent().getId())
                .toList();
        List<StudySession> studySessions = study.getStudySessions();

        Map<Long, List<StudyAchievement>> studyAchievementMap = getStudyAchievementMap(studyId, studentIds);
        Map<Long, List<Attendance>> attendanceMap = getAttendanceMap(studyId, studentIds);
        Map<Long, List<AssignmentHistory>> assignmentHistoryMap = getAssignmentHistoryMap(studyId, studentIds);

        List<MentorStudyStudentResponse> content = new ArrayList<>();

        studyHistories.forEach(studyHistory -> {
            List<StudyAchievement> currentStudyAchievements =
                    studyAchievementMap.getOrDefault(studyHistory.getStudent().getId(), List.of());
            List<Attendance> currentAttendances =
                    attendanceMap.getOrDefault(studyHistory.getStudent().getId(), List.of());
            List<AssignmentHistory> currentAssignmentHistories =
                    assignmentHistoryMap.getOrDefault(studyHistory.getStudent().getId(), List.of());

            List<StudyTaskDto> studyTasks = new ArrayList<>();
            studySessions.forEach(studySession -> {
                studyTasks.add(StudyTaskDto.of(studySession, type, isAttended(currentAttendances, studySession), now));
                studyTasks.add(StudyTaskDto.of(
                        studySession, getSubmittedAssignment(currentAssignmentHistories, studySession), now));
            });

            content.add(MentorStudyStudentResponse.of(studyHistory, currentStudyAchievements, studyTasks));
        });

        return excelUtil.createStudyExcel(study, content);
    }

    private StudyRoundStatisticsDto calculateRoundStatistics(StudySession studySession, Long totalStudentCount) {
        long attendanceCount = attendanceRepository.countByStudySessionId(studySession.getId());
        long attendanceRate = Math.round(attendanceCount / (double) totalStudentCount * 100);

        long successfullySubmittedAssignmentCount =
                assignmentHistoryRepository.countByStudySessionIdAndSubmissionStatusEquals(
                        studySession.getId(), SUCCESS);
        long assignmentSubmissionRate =
                Math.round(successfullySubmittedAssignmentCount / (double) totalStudentCount * 100);

        return StudyRoundStatisticsDto.of(studySession.getPosition(), attendanceRate, assignmentSubmissionRate);
    }

    private long calculateAverageWeekAttendanceRate(List<StudyRoundStatisticsDto> studyRoundStatisticsDtos) {

        double averageAttendanceRate = studyRoundStatisticsDtos.stream()
                .mapToLong(StudyRoundStatisticsDto::attendanceRate)
                .average()
                .orElse(0);

        return Math.round(averageAttendanceRate);
    }

    private long calculateAverageWeekAssignmentSubmissionRate(List<StudyRoundStatisticsDto> studyRoundStatisticsDtos) {

        double averageAssignmentSubmissionRate = studyRoundStatisticsDtos.stream()
                .mapToLong(StudyRoundStatisticsDto::assignmentSubmissionRate)
                .average()
                .orElse(0);

        return Math.round(averageAssignmentSubmissionRate);
    }

    private Map<Long, List<StudyAchievement>> getStudyAchievementMap(Long studyId, List<Long> studentIds) {
        List<StudyAchievement> studyAchievements =
                studyAchievementRepository.findByStudyIdAndMemberIds(studyId, studentIds);
        return studyAchievements.stream()
                .collect(groupingBy(
                        studyAchievement -> studyAchievement.getStudent().getId()));
    }

    private Map<Long, List<Attendance>> getAttendanceMap(Long studyId, List<Long> studentIds) {
        List<Attendance> attendances = attendanceRepository.findByStudyIdAndMemberIds(studyId, studentIds);
        return attendances.stream()
                .collect(groupingBy(attendance -> attendance.getStudent().getId()));
    }

    private Map<Long, List<AssignmentHistory>> getAssignmentHistoryMap(Long studyId, List<Long> studentIds) {
        List<AssignmentHistory> assignmentHistories =
                assignmentHistoryRepository.findByStudyIdAndMemberIds(studyId, studentIds);
        return assignmentHistories.stream()
                .collect(groupingBy(
                        assignmentHistory -> assignmentHistory.getMember().getId()));
    }

    private boolean isAttended(List<Attendance> attendances, StudySession studySession) {
        return attendances.stream()
                .anyMatch(attendance -> attendance.getStudySession().getId().equals(studySession.getId()));
    }

    private AssignmentHistory getSubmittedAssignment(
            List<AssignmentHistory> assignmentHistories, StudySession studySession) {
        return assignmentHistories.stream()
                .filter(assignmentHistory ->
                        assignmentHistory.getStudySession().getId().equals(studySession.getId()))
                .findFirst()
                .orElse(null);
    }
}
