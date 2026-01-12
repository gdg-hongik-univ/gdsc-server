package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.StudentStudyService;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySimpleDto;
import com.gdschongik.gdsc.domain.study.dto.response.StudyApplicableResponse;
import com.gdschongik.gdsc.domain.study.dto.response.StudyDashboardResponse;
import com.gdschongik.gdsc.domain.study.dto.response.StudyTodoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study - Student", description = "학생 스터디 API입니다.")
@RestController
@RequestMapping("/v2/studies")
@RequiredArgsConstructor
public class StudentStudyController {

    private final StudentStudyService studentStudyService;

    @Operation(summary = "내 스터디 대시보드 조회", description = "나의 스터디 대시보드를 조회합니다.")
    @GetMapping("/{studyId}/me/dashboard")
    public ResponseEntity<StudyDashboardResponse> getMyStudyDashboard(@PathVariable Long studyId) {
        var response = studentStudyService.getMyStudyDashboard(studyId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "나의 신청 가능한 스터디 조회", description = "현재 모집 중인 스터디와 내가 신청한 스터디를 조회합니다.")
    @GetMapping("/applicable/me")
    public ResponseEntity<StudyApplicableResponse> getAllApplicableStudies() {
        var response = studentStudyService.getAllApplicableStudies();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "내 수강 중인 스터디 조회", description = "나의 이번 학기 수강 중인 스터디를 조회합니다.")
    @GetMapping("/me/ongoing")
    public ResponseEntity<List<StudySimpleDto>> getMyCurrentStudies() {
        var response = studentStudyService.getMyCurrentStudies();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "수강중인 특정 스터디의 할 일 리스트 조회", description = "나의 수강중인 특정 스터디의 할 일 리스트를 조회합니다.")
    @GetMapping("/{studyId}/me/todos")
    public ResponseEntity<List<StudyTodoResponse>> getStudyTodoList(@PathVariable Long studyId) {
        var response = studentStudyService.getMyStudyTodos(studyId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "수강중인 모든 스터디의 할 일 리스트 조회", description = "나의 수강중인 모든 스터디의 할 일 리스트를 조회합니다.")
    @GetMapping("/me/todos")
    public ResponseEntity<List<StudyTodoResponse>> getStudiesTodoList() {
        var response = studentStudyService.getMyStudiesTodos();
        return ResponseEntity.ok().body(response);
    }
}
