package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.MentorStudyAnnouncementService;
import com.gdschongik.gdsc.domain.study.dto.request.StudyAnnouncementCreateRequest;
import com.gdschongik.gdsc.domain.study.dto.request.StudyAnnouncementCreateUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study Announcement - Mentor", description = "스터디 공지 멘토 API입니다.")
@RestController
@RequestMapping("/mentor/study-announcements")
@RequiredArgsConstructor
public class MentorStudyAnnouncementController {

    private final MentorStudyAnnouncementService mentorStudyAnnouncementService;

    @Operation(summary = "스터디 공지 생성", description = "스터디의 공지사항을 생성합니다.")
    @PostMapping
    public ResponseEntity<Void> createStudyAnnouncement(@Valid @RequestBody StudyAnnouncementCreateRequest request) {
        mentorStudyAnnouncementService.createStudyAnnouncement(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스터디 공지 수정", description = "스터디의 공지사항을 수정합니다.")
    @PutMapping("/{studyAnnouncementId}")
    public ResponseEntity<Void> updateStudyAnnouncement(
            @PathVariable Long studyAnnouncementId, @Valid @RequestBody StudyAnnouncementCreateUpdateRequest request) {
        mentorStudyAnnouncementService.updateStudyAnnouncement(studyAnnouncementId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스터디 공지 삭제", description = "스터디의 공지사항을 삭제합니다.")
    @DeleteMapping("/{studyAnnouncementId}")
    public ResponseEntity<Void> deleteStudyAnnouncement(@PathVariable Long studyAnnouncementId) {
        mentorStudyAnnouncementService.deleteStudyAnnouncement(studyAnnouncementId);
        return ResponseEntity.ok().build();
    }
}
