package com.gdschongik.gdsc.domain.studyv2.dto.response;

import static com.gdschongik.gdsc.domain.studyv2.dto.dto.StudyTaskDto.StudyTaskType.*;

import com.gdschongik.gdsc.domain.member.dto.MemberInfoDto;
import com.gdschongik.gdsc.domain.studyv2.domain.AssignmentHistoryStatus;
import com.gdschongik.gdsc.domain.studyv2.domain.AttendanceStatus;
import com.gdschongik.gdsc.domain.studyv2.domain.StudyAchievementV2;
import com.gdschongik.gdsc.domain.studyv2.domain.StudyHistoryV2;
import com.gdschongik.gdsc.domain.studyv2.dto.dto.StudyAchievementDto;
import com.gdschongik.gdsc.domain.studyv2.dto.dto.StudyHistoryDto;
import com.gdschongik.gdsc.domain.studyv2.dto.dto.StudyTaskDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record MentorStudyStudentResponse(
        MemberInfoDto member,
        StudyHistoryDto studyHistory,
        List<StudyAchievementDto> achievements,
        @Schema(description = "과제 및 출석 이력") List<StudyTaskDto> studyTasks,
        @Schema(description = "과제 수행률") double assignmentRate,
        @Schema(description = "출석률") double attendanceRate) {

    public static MentorStudyStudentResponse of(
            StudyHistoryV2 studyHistory, List<StudyAchievementV2> studyAchievements, List<StudyTaskDto> studyTasks) {
        List<StudyTaskDto> assignments = studyTasks.stream()
                .filter(studyTaskDto -> studyTaskDto.taskType() == ASSIGNMENT)
                .toList();

        List<StudyTaskDto> attendances = studyTasks.stream()
                .filter(studyTaskDto -> studyTaskDto.taskType() == ATTENDANCE)
                .toList();

        long successAssignmentsCount = countAssignmentByStatus(assignments);
        long attendedCount = countAttendanceByStatus(attendances);

        return new MentorStudyStudentResponse(
                MemberInfoDto.from(studyHistory.getStudent()),
                StudyHistoryDto.from(studyHistory),
                studyAchievements.stream().map(StudyAchievementDto::from).toList(),
                studyTasks,
                calculateRateOrZero(successAssignmentsCount, assignments.size()),
                calculateRateOrZero(attendedCount, attendances.size()));
    }

    private static long countAssignmentByStatus(List<StudyTaskDto> assignments) {
        return assignments.stream()
                .filter(studyTaskDto -> studyTaskDto.assignmentSubmissionStatus() == AssignmentHistoryStatus.SUCCEEDED)
                .count();
    }

    private static long countAttendanceByStatus(List<StudyTaskDto> attendances) {
        return attendances.stream()
                .filter(studyTaskDto -> studyTaskDto.attendanceStatus() == AttendanceStatus.ATTENDED)
                .count();
    }

    private static double calculateRateOrZero(long dividend, long divisor) {
        return divisor == 0 ? 0 : (double) dividend * 100 / divisor;
    }
}
