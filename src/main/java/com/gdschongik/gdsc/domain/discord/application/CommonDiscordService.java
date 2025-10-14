package com.gdschongik.gdsc.domain.discord.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.RequirementStatus;
import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.discord.domain.service.DiscordValidator;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.studyv2.dao.StudyAnnouncementV2Repository;
import com.gdschongik.gdsc.domain.studyv2.dao.StudyHistoryV2Repository;
import com.gdschongik.gdsc.domain.studyv2.dao.StudyV2Repository;
import com.gdschongik.gdsc.domain.studyv2.domain.StudyAnnouncementV2;
import com.gdschongik.gdsc.domain.studyv2.domain.StudyHistoryV2;
import com.gdschongik.gdsc.domain.studyv2.domain.StudyV2;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.DiscordUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonDiscordService {

    private final MemberRepository memberRepository;
    private final StudyAnnouncementV2Repository studyAnnouncementV2Repository;
    private final StudyV2Repository studyV2Repository;
    private final StudyHistoryV2Repository studyHistoryV2Repository;
    private final DiscordUtil discordUtil;
    private final DiscordValidator discordValidator;

    public String getNicknameByDiscordUsername(String discordUsername) {
        return memberRepository
                .findByDiscordUsername(discordUsername)
                .map(Member::getNickname)
                .orElse(null);
    }

    @Transactional
    public void batchDiscordId(String currentDiscordUsername, RequirementStatus discordStatus) {
        Member currentMember = memberRepository
                .findByDiscordUsername(currentDiscordUsername)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        discordValidator.validateAdminPermission(currentMember);

        List<Member> discordSatisfiedMembers = memberRepository.findAllByDiscordStatus(discordStatus);

        discordSatisfiedMembers.forEach(member -> {
            String discordUsername = member.getDiscordUsername();
            try {
                String discordId = discordUtil.getMemberIdByUsername(discordUsername);
                member.updateDiscordId(discordId);
            } catch (CustomException e) {
                log.info("[CommonDiscordService] 디스코드 id 배치 실패: 사유 = {} memberId = {}", e.getMessage(), member.getId());
            }
        });
    }

    @Transactional
    public void assignDiscordStudyRole(
            String currentMemberDiscordUsername, String studyTitle, Integer academicYear, String semester) {
        Member currentMember = memberRepository
                .findByDiscordUsername(currentMemberDiscordUsername)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        discordValidator.validateAdminPermission(currentMember);

        StudyV2 study = studyV2Repository
                .findByTitleAndSemester(studyTitle, Semester.of(academicYear, SemesterType.valueOf(semester)))
                .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        List<StudyHistoryV2> studyHistories = studyHistoryV2Repository.findAllByStudy(study);
        studyHistories.forEach(studyHistoryV2 -> discordUtil.addRoleToMemberById(
                study.getDiscordRoleId(), studyHistoryV2.getStudent().getDiscordId()));

        log.info("[CommonDiscordService] 스터디 디스코드 역할 부여 완료: studyId = {}", study.getId());
    }

    @Transactional
    public void addStudyRoleToMember(String studyDiscordRoleId, String memberDiscordId) {
        discordUtil.addRoleToMemberById(studyDiscordRoleId, memberDiscordId);
    }

    @Transactional
    public void removeStudyRoleFromMember(String studyDiscordRoleId, String memberDiscordId) {
        discordUtil.removeRoleFromMemberById(studyDiscordRoleId, memberDiscordId);
    }

    @Transactional
    public void sendStudyAnnouncement(Long studyAnnouncementId) {
        StudyAnnouncementV2 studyAnnouncement = studyAnnouncementV2Repository
                .findById(studyAnnouncementId)
                .orElseThrow(() -> new CustomException(STUDY_ANNOUNCEMENT_NOT_FOUND));

        discordUtil.sendStudyAnnouncementToChannel(
                studyAnnouncement.getStudy().getDiscordChannelId(),
                studyAnnouncement.getStudy().getDiscordRoleId(),
                studyAnnouncement.getStudy().getTitle(),
                studyAnnouncement.getTitle(),
                studyAnnouncement.getLink(),
                studyAnnouncement.getCreatedAt());

        log.info("[CommonDiscordService] 스터디 공지 전송 완료: studyAnnouncementId = {}", studyAnnouncementId);
    }
}
