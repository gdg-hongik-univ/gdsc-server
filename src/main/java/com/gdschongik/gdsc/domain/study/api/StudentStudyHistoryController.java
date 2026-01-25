package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.StudentStudyHistoryService;
import com.gdschongik.gdsc.domain.study.dto.request.StudyApplyCancelRequest;
import com.gdschongik.gdsc.domain.study.dto.request.StudyApplyRequest;
import com.gdschongik.gdsc.domain.study.dto.request.StudyHistoryRepositoryUpdateRequest;
import com.gdschongik.gdsc.domain.study.dto.response.StudyHistoryMyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study History - Student", description = "학생 스터디 수강이력 API입니다.")
@RestController
@RequestMapping("/study-histories")
@RequiredArgsConstructor
public class StudentStudyHistoryController {

    private final StudentStudyHistoryService studentStudyHistoryService;

    @Operation(summary = "내 레포지토리 입력", description = "나의 과제 제출 레포지토리를 입력합니다.")
    @PutMapping("/repositories/me")
    public ResponseEntity<Void> updateMyRepository(@Valid @RequestBody StudyHistoryRepositoryUpdateRequest request) {
        studentStudyHistoryService.updateMyRepository(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 스터디 수강이력 목록 조회", description = "나의 스터디 수강이력 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<List<StudyHistoryMyResponse>> getMyStudyHistories() {
        var response = studentStudyHistoryService.getMyStudyHistories();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 수강신청", description = "모집중인 스터디에 수강신청 합니다. 여러 스터디에 수강신청 할 수 있습니다.")
    @PostMapping
    public ResponseEntity<Void> applyStudy(@Valid @RequestBody StudyApplyRequest request) {
        studentStudyHistoryService.applyStudy(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스터디 수강신청 취소", description = "수강신청을 취소합니다. 스터디 수강신청 기간 중에만 취소할 수 있습니다.")
    @DeleteMapping
    public ResponseEntity<Void> cancelStudyApply(@Valid @RequestBody StudyApplyCancelRequest request) {
        studentStudyHistoryService.cancelStudyApply(request);
        return ResponseEntity.noContent().build();
    }
}
