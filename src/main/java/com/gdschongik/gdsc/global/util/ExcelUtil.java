package com.gdschongik.gdsc.global.util;

import static com.gdschongik.gdsc.domain.member.domain.MemberRole.*;
import static com.gdschongik.gdsc.global.common.constant.WorkbookConstant.*;

import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Department;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.member.dto.MemberInfoDto;
import com.gdschongik.gdsc.domain.study.domain.AchievementType;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyAchievementDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyHistoryDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyTaskDto;
import com.gdschongik.gdsc.domain.study.dto.response.MentorStudyStudentResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import jakarta.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExcelUtil {

    private final MemberRepository memberRepository;

    public byte[] createMemberExcel() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        createMemberSheetByRole(workbook, ALL_MEMBER_SHEET_NAME, null);
        createMemberSheetByRole(workbook, REGULAR_MEMBER_SHEET_NAME, REGULAR);
        return createByteArray(workbook);
    }

    public byte[] createStudyExcel(Study study, List<MentorStudyStudentResponse> content) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        createStudySheet(workbook, study, content);
        return createByteArray(workbook);
    }

    private void createMemberSheetByRole(Workbook workbook, String sheetName, @Nullable MemberRole role) {
        Sheet sheet = setUpMemberSheet(workbook, sheetName);

        memberRepository.findAllByRole(role).forEach(member -> {
            Row memberRow = sheet.createRow(sheet.getLastRowNum() + 1);
            int cellIndex = 0;

            memberRow.createCell(cellIndex++).setCellValue(member.getCreatedAt().toString());
            memberRow.createCell(cellIndex++).setCellValue(member.getName());
            memberRow.createCell(cellIndex++).setCellValue(member.getStudentId());
            memberRow
                    .createCell(cellIndex++)
                    .setCellValue(Optional.ofNullable(member.getDepartment())
                            .map(Department::getDepartmentName)
                            .orElse(""));
            memberRow.createCell(cellIndex++).setCellValue(member.getPhone());
            memberRow.createCell(cellIndex++).setCellValue(member.getEmail());
            memberRow.createCell(cellIndex++).setCellValue(member.getDiscordUsername());
            memberRow.createCell(cellIndex++).setCellValue(member.getNickname());
        });
    }

    private void createStudySheet(Workbook workbook, Study study, List<MentorStudyStudentResponse> content) {
        Sheet sheet = setUpStudySheet(workbook, study.getTitle(), study.getTotalRound());

        content.forEach(student -> {
            MemberInfoDto member = student.member();
            StudyHistoryDto studyHistory = student.studyHistory();
            List<StudyAchievementDto> studyAchievements = student.achievements();
            boolean isFirstRoundOutstandingStudent = studyAchievements.stream()
                    .anyMatch(studyAchievementDto ->
                            studyAchievementDto.type() == AchievementType.FIRST_ROUND_OUTSTANDING_STUDENT);
            boolean isSecondRoundOutstandingStudent = studyAchievements.stream()
                    .anyMatch(studyAchievementDto ->
                            studyAchievementDto.type() == AchievementType.SECOND_ROUND_OUTSTANDING_STUDENT);

            Row studentRow = sheet.createRow(sheet.getLastRowNum() + 1);
            AtomicInteger cellIndex = new AtomicInteger(0);

            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(member.name());
            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(member.studentId());
            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(member.discordUsername());
            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(member.nickname());
            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(studyHistory.githubLink());
            studentRow
                    .createCell(cellIndex.getAndIncrement())
                    .setCellValue(studyHistory.status().getValue());
            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(isFirstRoundOutstandingStudent ? "O" : "X");
            studentRow
                    .createCell(cellIndex.getAndIncrement())
                    .setCellValue(isSecondRoundOutstandingStudent ? "O" : "X");
            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(student.attendanceRate());
            studentRow.createCell(cellIndex.getAndIncrement()).setCellValue(student.assignmentRate());
            student.studyTasks().stream()
                    .filter(studyTaskDto -> studyTaskDto.taskType() == StudyTaskDto.StudyTaskType.ASSIGNMENT)
                    .forEach(task -> studentRow
                            .createCell(cellIndex.getAndIncrement())
                            .setCellValue(task.assignmentSubmissionStatus().getValue()));
            student.studyTasks().stream()
                    .filter(studyTaskDto -> studyTaskDto.taskType() == StudyTaskDto.StudyTaskType.ATTENDANCE)
                    .forEach(task -> studentRow
                            .createCell(cellIndex.getAndIncrement())
                            .setCellValue(task.attendanceStatus().getValue()));
        });
    }

    private Sheet setUpMemberSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row row = sheet.createRow(0);
        IntStream.range(0, MEMBER_SHEET_HEADER.length).forEach(i -> {
            Cell cell = row.createCell(i);
            cell.setCellValue(MEMBER_SHEET_HEADER[i]);
        });
        return sheet;
    }

    private Sheet setUpStudySheet(Workbook workbook, String sheetName, long totalWeek) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row row = sheet.createRow(0);
        IntStream.range(0, STUDY_SHEET_HEADER.length).forEach(i -> {
            Cell cell = row.createCell(i);
            cell.setCellValue(STUDY_SHEET_HEADER[i]);
        });

        for (int i = 1; i <= totalWeek; i++) {
            Cell cell = row.createCell(row.getLastCellNum());
            cell.setCellValue(String.format(WEEKLY_ASSIGNMENT, i));
        }

        for (int i = 1; i <= totalWeek; i++) {
            Cell cell = row.createCell(row.getLastCellNum());
            cell.setCellValue(String.format(WEEKLY_ATTENDANCE, i));
        }
        return sheet;
    }

    private byte[] createByteArray(Workbook workbook) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.EXCEL_WORKSHEET_WRITE_FAILED);
        }
        return outputStream.toByteArray();
    }
}
