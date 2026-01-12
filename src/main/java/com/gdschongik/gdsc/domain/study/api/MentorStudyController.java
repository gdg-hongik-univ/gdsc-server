package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.MentorStudyService;
import com.gdschongik.gdsc.domain.study.dto.request.StudyUpdateRequest;
import com.gdschongik.gdsc.domain.study.dto.response.MentorStudyStudentResponse;
import com.gdschongik.gdsc.domain.study.dto.response.StudyManagerResponse;
import com.gdschongik.gdsc.domain.study.dto.response.StudyStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study - Mentor", description = "스터디 멘토 API입니다.")
@RestController
@RequestMapping("/v2/mentor/studies")
@RequiredArgsConstructor
public class MentorStudyController {

    private final MentorStudyService mentorStudyService;

    @Operation(summary = "내 스터디 조회", description = "내가 멘토로 있는 스터디를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<List<StudyManagerResponse>> getStudiesInCharge() {
        var response = mentorStudyService.getStudiesInCharge();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 정보 변경", description = "스터디 정보를 변경합니다.")
    @PutMapping("/{studyId}")
    public ResponseEntity<Void> updateStudy(
            @PathVariable Long studyId, @RequestBody @Valid StudyUpdateRequest request) {
        mentorStudyService.updateStudy(studyId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스터디 통계 조회", description = "멘토가 자신의 스터디 출석률, 과제 제출률, 수료율에 대한 통계를 조회합니다.")
    @GetMapping("/{studyId}/statistics")
    public ResponseEntity<StudyStatisticsResponse> getStudyStatistics(@PathVariable Long studyId) {
        var response = mentorStudyService.getStudyStatistics(studyId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 수강생 관리", description = "해당 스터디의 수강생을 관리합니다")
    @GetMapping("/{studyId}/students")
    public ResponseEntity<Page<MentorStudyStudentResponse>> getStudyStudents(
            @PathVariable Long studyId, Pageable pageable) {
        var response = mentorStudyService.getStudyStudents(studyId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "수강생 정보 엑셀 다운로드", description = "수강생 정보를 엑셀로 다운로드합니다.")
    @GetMapping("/{studyId}/students/excel")
    public ResponseEntity<byte[]> createStudyWorkbook(@PathVariable Long studyId) {
        byte[] response = mentorStudyService.createStudyExcel(studyId);
        ContentDisposition contentDisposition =
                ContentDisposition.builder("attachment").filename("study.xls").build();
        return ResponseEntity.ok()
                .headers(httpHeaders -> {
                    httpHeaders.setContentDisposition(contentDisposition);
                    httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    httpHeaders.setContentLength(response.length);
                })
                .body(response);
    }
}
