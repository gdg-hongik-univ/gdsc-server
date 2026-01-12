package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.MentorStudyAchievementService;
import com.gdschongik.gdsc.domain.study.dto.request.OutstandingStudentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study Achievement - Mentor", description = "멘토 스터디 우수 스터디원 관리 API입니다.")
@RestController
@RequestMapping("/v2/mentor/study-achievements")
@RequiredArgsConstructor
public class MentorStudyAchievementController {

    private final MentorStudyAchievementService mentorStudyAchievementService;

    @Operation(summary = "우수 스터디원 지정", description = "우수 스터디원으로 지정합니다.")
    @PostMapping
    public ResponseEntity<Void> designateOutstandingStudent(
            @RequestParam(name = "studyId") Long studyId, @RequestBody OutstandingStudentRequest request) {
        mentorStudyAchievementService.designateOutstandingStudent(studyId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "우수 스터디원 철회", description = "우수 스터디원 지정을 철회합니다.")
    @DeleteMapping
    public ResponseEntity<Void> withdrawOutstandingStudent(
            @RequestParam(name = "studyId") Long studyId, @RequestBody OutstandingStudentRequest request) {
        mentorStudyAchievementService.withdrawOutstandingStudent(studyId, request);
        return ResponseEntity.ok().build();
    }
}
