package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.MentorStudyAnnouncementService;
import com.gdschongik.gdsc.domain.study.dto.request.NotionWebhookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study Webhook", description = "스터디 웹훅 API입니다.")
@RestController
@RequestMapping("/webhook/study")
@RequiredArgsConstructor
public class StudyWebhookController {

    private final MentorStudyAnnouncementService mentorStudyAnnouncementService;

    @Operation(summary = "스터디 노션 웹훅 처리", description = "공지 및 과제 발행 요청 웹훅을 처리합니다.")
    @PostMapping
    public ResponseEntity<Void> handleStudyWebhook(@RequestBody NotionWebhookRequest request) {
        mentorStudyAnnouncementService.createStudyAnnouncementByWebhook(request);
        return ResponseEntity.ok().build();
    }
}
