package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.common.constant.UrlConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.StudyAnnouncementRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyAnnouncement;
import com.gdschongik.gdsc.domain.study.domain.service.StudyValidator;
import com.gdschongik.gdsc.domain.study.dto.request.NotionWebhookRequest;
import com.gdschongik.gdsc.domain.study.dto.request.StudyAnnouncementCreateRequest;
import com.gdschongik.gdsc.domain.study.dto.request.StudyAnnouncementCreateUpdateRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorStudyAnnouncementService {

    private final MemberUtil memberUtil;
    private final StudyValidator studyValidator;
    private final StudyRepository studyRepository;
    private final StudyAnnouncementRepository studyAnnouncementRepository;

    @Transactional
    public void createStudyAnnouncement(StudyAnnouncementCreateRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study =
                studyRepository.findById(request.studyId()).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        studyValidator.validateStudyMentor(currentMember, study);

        StudyAnnouncement studyAnnouncement = StudyAnnouncement.create(request.title(), request.link(), study);
        studyAnnouncementRepository.save(studyAnnouncement);

        log.info("[MentorStudyAnnouncementService] 스터디 공지 생성: studyAnnouncementId={}", studyAnnouncement.getId());
    }

    @Transactional
    public void updateStudyAnnouncement(Long studyAnnouncementId, StudyAnnouncementCreateUpdateRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        StudyAnnouncement studyAnnouncement = studyAnnouncementRepository
                .findById(studyAnnouncementId)
                .orElseThrow(() -> new CustomException(STUDY_ANNOUNCEMENT_NOT_FOUND));
        Study study = studyAnnouncement.getStudy();

        studyValidator.validateStudyMentor(currentMember, study);

        studyAnnouncement.update(request.title(), request.link());
        studyAnnouncementRepository.save(studyAnnouncement);

        log.info("[MentorStudyAnnouncementService] 스터디 공지 수정 완료: studyAnnouncementId={}", studyAnnouncement.getId());
    }

    @Transactional
    public void deleteStudyAnnouncement(Long studyAnnouncementId) {
        Member currentMember = memberUtil.getCurrentMember();
        StudyAnnouncement studyAnnouncement = studyAnnouncementRepository
                .findById(studyAnnouncementId)
                .orElseThrow(() -> new CustomException(STUDY_ANNOUNCEMENT_NOT_FOUND));
        Study study = studyAnnouncement.getStudy();

        studyValidator.validateStudyMentor(currentMember, study);

        studyAnnouncementRepository.delete(studyAnnouncement);

        log.info("[MentorStudyAnnouncementService] 스터디 공지 삭제 완료: studyAnnouncementId={}", studyAnnouncement.getId());
    }

    @Transactional
    public void createStudyAnnouncementByWebhook(NotionWebhookRequest request) {
        String studyName = request.getStudyName();
        Semester semester = request.getSemester();

        Study study = studyRepository
                .findByTitleAndSemester(studyName, semester)
                .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        String announcementTitle = request.getTitle();
        String fullLink = STUDY_ANNOUNCEMENT_DOMAIN + request.getCleanUrl();

        StudyAnnouncement studyAnnouncement = StudyAnnouncement.create(announcementTitle, fullLink, study);
        studyAnnouncementRepository.save(studyAnnouncement);

        log.info(
                "[MentorStudyAnnouncementService] 노션 웹훅으로 스터디 공지 생성: studyAnnouncementId={}, title={}, link={}",
                studyAnnouncement.getId(),
                announcementTitle,
                fullLink);
    }
}
