package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.AdminStudyService;
import com.gdschongik.gdsc.domain.study.dto.request.StudyCreateRequest;
import com.gdschongik.gdsc.domain.study.dto.response.StudyManagerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study - Admin", description = "스터디 어드민 API입니다.")
@RestController
@RequestMapping("/v2/admin/studies")
@RequiredArgsConstructor
public class AdminStudyController {

    private final AdminStudyService adminStudyService;

    @Operation(summary = "스터디 개설", description = "스터디를 개설합니다. 빈 스터디회차를 함께 생성합니다. 과제 스터디의 경우 라이브 세션 관련 필드는 null이어야 합니다.")
    @PostMapping
    public ResponseEntity<Void> createStudy(@Valid @RequestBody StudyCreateRequest request) {
        adminStudyService.createStudy(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 스터디 조회", description = "모든 스터디를 조회합니다. 코어멤버만 접근 가능합니다.")
    @GetMapping
    public ResponseEntity<List<StudyManagerResponse>> getStudies() {
        var response = adminStudyService.getAllStudies();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 삭제", description = "스터디를 삭제합니다. 스터디의 세션도 함께 삭제됩니다.")
    @DeleteMapping("/{studyId}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Long studyId) {
        adminStudyService.deleteStudy(studyId);
        return ResponseEntity.ok().build();
    }
}
