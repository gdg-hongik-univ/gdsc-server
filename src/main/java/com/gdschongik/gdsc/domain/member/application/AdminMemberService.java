package com.gdschongik.gdsc.domain.member.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.member.domain.service.MemberValidator;
import com.gdschongik.gdsc.domain.member.dto.request.MemberDemoteRequest;
import com.gdschongik.gdsc.domain.member.dto.request.MemberQueryOption;
import com.gdschongik.gdsc.domain.member.dto.request.MemberUpdateRequest;
import com.gdschongik.gdsc.domain.member.dto.response.AdminMemberResponse;
import com.gdschongik.gdsc.domain.recruitment.dao.RecruitmentRepository;
import com.gdschongik.gdsc.domain.recruitment.dao.RecruitmentRoundRepository;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.ExcelUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final ExcelUtil excelUtil;
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentRoundRepository recruitmentRoundRepository;
    private final MemberValidator memberValidator;

    public Page<AdminMemberResponse> searchMembers(MemberQueryOption queryOption, Pageable pageable) {
        Page<Member> members = memberRepository.searchMembers(queryOption, pageable);
        return members.map(AdminMemberResponse::from);
    }

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        member.withdraw();
    }

    @Transactional
    public void updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        member.updateInfo(
                request.studentId(),
                request.name(),
                request.phone(),
                request.department(),
                request.email(),
                request.discordUsername(),
                request.nickname());
    }

    public byte[] createExcel() {
        return excelUtil.createMemberExcel();
    }

    @Transactional
    public void demoteAllRegularMembersToAssociate(MemberDemoteRequest request) {
        List<RecruitmentRound> recruitmentRounds = recruitmentRoundRepository.findAllBySemester(
                Semester.of(request.academicYear(), request.semesterType()));
        LocalDateTime now = LocalDateTime.now();

        memberValidator.validateMemberDemote(recruitmentRounds, now);

        List<Member> regularMembers = memberRepository.findAllByRole(MemberRole.REGULAR);

        regularMembers.forEach(Member::demoteToAssociate);

        memberRepository.saveAll(regularMembers);

        log.info(
                "[AdminMemberService] 정회원 일괄 강등: demotedMemberIds={}",
                regularMembers.stream().map(Member::getId).toList());
    }

    @Transactional
    public void advanceAllAdvanceFailedMembersToRegular(String discordUsername) {
        Member currentMember = memberRepository
                .findByDiscordUsername(discordUsername)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        memberValidator.validateAdminPermission(currentMember.getManageRole());

        LocalDateTime now = LocalDateTime.now();
        Recruitment recruitment = recruitmentRepository
                .findCurrentRecruitment(now)
                .orElseThrow(() -> new CustomException(RECRUITMENT_NOT_FOUND));

        List<Member> advanceFailedMembers = memberRepository.findAllAdvanceFailedMembers(recruitment.getSemester());
        advanceFailedMembers.forEach(Member::advanceToRegular);
        memberRepository.saveAll(advanceFailedMembers);

        log.info(
                "[AdminMemberService] 정회원 승급 누락 멤버들을 정회원으로 승급: advancedMemberIds={}",
                advanceFailedMembers.stream().map(Member::getId).toList());
    }

    @Transactional
    public void assignAdminRole(String currentMemberDiscordUsername, String studentId) {
        Member currentMember = memberRepository
                .findByDiscordUsername(currentMemberDiscordUsername)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        memberValidator.validateAdminPermission(currentMember.getManageRole());

        Member memberToAssign =
                memberRepository.findByStudentId(studentId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        memberToAssign.assignToAdmin();
        memberRepository.save(memberToAssign);

        log.info("[AdminMemberService] 어드민 권한 부여: memberId={}", memberToAssign.getId());
    }
}
