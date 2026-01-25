package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.StudentStudyAnnouncementService;
import com.gdschongik.gdsc.domain.study.dto.response.StudyAnnouncementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study Announcement - Student", description = "학생 스터디 공지 API입니다.")
@RestController
@RequestMapping("/study-announcements")
@RequiredArgsConstructor
public class StudentStudyAnnouncementController {

    private final StudentStudyAnnouncementService studentStudyAnnouncementService;

    @Operation(summary = "수강중인 특정 스터디의 공지 목록 조회", description = "나의 수강중인 특정 스터디의 공지 목록을 조회합니다.")
    @GetMapping("/{studyId}/me")
    public ResponseEntity<List<StudyAnnouncementResponse>> getStudyAnnouncements(@PathVariable Long studyId) {
        var response = studentStudyAnnouncementService.getStudyAnnouncements(studyId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "수강중인 모든 스터디의 공지 목록 조회", description = "나의 수강중인 모든 스터디의 공지 목록을 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<List<StudyAnnouncementResponse>> getStudiesAnnouncements() {
        var response = studentStudyAnnouncementService.getStudiesAnnouncements();
        return ResponseEntity.ok(response);
    }
}
