package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.recruitment.dao.RecruitmentRepository;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.study.dao.StudyAnnouncementRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.domain.StudyAnnouncement;
import com.gdschongik.gdsc.domain.study.dto.response.StudyAnnouncementResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentStudyAnnouncementService {

    private final MemberUtil memberUtil;
    private final RecruitmentRepository recruitmentRepository;
    private final StudyAnnouncementRepository studyAnnouncementRepository;
    private final StudyHistoryRepository studyHistoryRepository;

    @Transactional(readOnly = true)
    public List<StudyAnnouncementResponse> getStudyAnnouncements(@Nullable Long studyId) {
        List<StudyAnnouncement> studyAnnouncements =
                studyAnnouncementRepository.findAllByStudyIdOrderByCreatedAtDesc(studyId);

        return studyAnnouncements.stream().map(StudyAnnouncementResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StudyAnnouncementResponse> getStudiesAnnouncements() {
        Member currentMember = memberUtil.getCurrentMember();
        LocalDateTime now = LocalDateTime.now();

        Recruitment recruitment = recruitmentRepository
                .findCurrentRecruitment(now)
                .orElseThrow(() -> new CustomException(RECRUITMENT_NOT_FOUND));

        List<Long> currentStudyIds = studyHistoryRepository.findAllByStudent(currentMember).stream()
                .filter(studyHistory -> studyHistory.getStudy().getSemester().equals(recruitment.getSemester()))
                .map(studyHistory -> studyHistory.getStudy().getId())
                .toList();

        List<StudyAnnouncement> studyAnnouncements =
                studyAnnouncementRepository.findAllByStudyIdsOrderByCreatedAtDesc(currentStudyIds);

        return studyAnnouncements.stream().map(StudyAnnouncementResponse::from).toList();
    }
}
