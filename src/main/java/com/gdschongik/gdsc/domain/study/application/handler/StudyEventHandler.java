package com.gdschongik.gdsc.domain.study.application.handler;

import com.gdschongik.gdsc.domain.discord.application.CommonDiscordService;
import com.gdschongik.gdsc.domain.study.application.CommonStudyService;
import com.gdschongik.gdsc.domain.study.domain.event.StudyAnnouncementCreatedEvent;
import com.gdschongik.gdsc.domain.study.domain.event.StudyApplyCanceledEvent;
import com.gdschongik.gdsc.domain.study.domain.event.StudyApplyCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyEventHandler {

    private final CommonDiscordService commonDiscordService;
    private final CommonStudyService commonStudyService;

    @ApplicationModuleListener
    public void handleStudyApplyCompletedEvent(StudyApplyCompletedEvent event) {
        log.info(
                "[StudyEventHandler] 수강신청 이벤트 수신: memberDiscordId={}, studyDiscordRoleId={}",
                event.memberDiscordId(),
                event.studyDiscordRoleId());

        commonDiscordService.addStudyRoleToMember(event.studyDiscordRoleId(), event.memberDiscordId());
    }

    @ApplicationModuleListener
    public void handleStudyApplyCanceledEvent(StudyApplyCanceledEvent event) {
        log.info(
                "[StudyEventHandler] 수강신청 취소 이벤트 수신: memberDiscordId={}, studyDiscordRoleId={}",
                event.memberDiscordId(),
                event.studyDiscordRoleId());

        commonDiscordService.removeStudyRoleFromMember(event.studyDiscordRoleId(), event.memberDiscordId());
        commonStudyService.deleteAttendance(event.studyId(), event.memberId());
        commonStudyService.deleteAssignmentHistory(event.studyId(), event.memberId());
    }

    @ApplicationModuleListener
    public void handleStudyAnnouncementCreatedEvent(StudyAnnouncementCreatedEvent event) {
        log.info("[StudyEventHandler] 스터디 공지사항 생성 이벤트 수신: studyAnnouncementId={}", event.studyAnnouncementId());

        commonDiscordService.sendStudyAnnouncement(event.studyAnnouncementId());
    }
}
