package com.gdschongik.gdsc.domain.studyv2.application.handler;

import com.gdschongik.gdsc.domain.discord.application.CommonDiscordService;
import com.gdschongik.gdsc.domain.studyv2.application.CommonStudyServiceV2;
import com.gdschongik.gdsc.domain.studyv2.domain.event.StudyAnnouncementCreatedEvent;
import com.gdschongik.gdsc.domain.studyv2.domain.event.StudyApplyCanceledEvent;
import com.gdschongik.gdsc.domain.studyv2.domain.event.StudyApplyCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyEventHandlerV2 {

    private final CommonDiscordService commonDiscordService;
    private final CommonStudyServiceV2 commonStudyServiceV2;

    @ApplicationModuleListener
    public void handleStudyApplyCompletedEvent(StudyApplyCompletedEvent event) {
        log.info(
                "[StudyEventHandlerV2] 수강신청 이벤트 수신: memberDiscordId={}, studyDiscordRoleId={}",
                event.memberDiscordId(),
                event.studyDiscordRoleId());

        commonDiscordService.addStudyRoleToMember(event.studyDiscordRoleId(), event.memberDiscordId());
    }

    @ApplicationModuleListener
    public void handleStudyApplyCanceledEvent(StudyApplyCanceledEvent event) {
        log.info(
                "[StudyEventHandlerV2] 수강신청 취소 이벤트 수신: memberDiscordId={}, studyDiscordRoleId={}",
                event.memberDiscordId(),
                event.studyDiscordRoleId());

        commonDiscordService.removeStudyRoleFromMember(event.studyDiscordRoleId(), event.memberDiscordId());
        commonStudyServiceV2.deleteAttendance(event.studyId(), event.memberId());
        commonStudyServiceV2.deleteAssignmentHistory(event.studyId(), event.memberId());
    }

    @ApplicationModuleListener
    public void handleStudyAnnouncementCreatedEvent(StudyAnnouncementCreatedEvent event) {
        log.info("[StudyEventHandlerV2] 스터디 공지사항 생성 이벤트 수신: studyAnnouncementId={}", event.studyAnnouncementId());

        commonDiscordService.sendStudyAnnouncement(event.studyAnnouncementId());
    }
}
