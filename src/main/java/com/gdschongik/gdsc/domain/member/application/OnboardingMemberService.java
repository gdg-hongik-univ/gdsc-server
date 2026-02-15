package com.gdschongik.gdsc.domain.member.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.application.UnivEmailVerificationService;
import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.EmailVerificationStatusService;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.dto.UnivVerificationStatus;
import com.gdschongik.gdsc.domain.member.dto.request.MemberInfoRequest;
import com.gdschongik.gdsc.domain.member.dto.response.MemberDashboardResponse;
import com.gdschongik.gdsc.domain.member.dto.response.MemberInfoResponse;
import com.gdschongik.gdsc.domain.member.dto.response.MemberPreviousInfoResponse;
import com.gdschongik.gdsc.domain.member.dto.response.MemberStudentIdDuplicateResponse;
import com.gdschongik.gdsc.domain.member.dto.response.MemberUnivStatusResponse;
import com.gdschongik.gdsc.domain.membership.application.MembershipService;
import com.gdschongik.gdsc.domain.membership.domain.Membership;
import com.gdschongik.gdsc.domain.recruitment.application.OnboardingRecruitmentService;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import com.gdschongik.gdsc.infra.github.client.GithubClient;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingMemberService {

    private final MemberUtil memberUtil;
    private final OnboardingRecruitmentService onboardingRecruitmentService;
    private final MembershipService membershipService;
    private final UnivEmailVerificationService univEmailVerificationService;
    private final MemberRepository memberRepository;
    private final EmailVerificationStatusService emailVerificationStatusService;
    private final GithubClient githubClient;

    public MemberUnivStatusResponse checkUnivVerificationStatus() {
        Member currentMember = memberUtil.getCurrentMember();
        return MemberUnivStatusResponse.from(currentMember);
    }

    @Transactional
    public void updateMemberInfo(MemberInfoRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        currentMember.updateInfo(
                request.studentId(), request.name(), request.phone(), request.department(), request.email());
        memberRepository.save(currentMember);
    }

    public MemberInfoResponse getMemberInfo() {
        Member currentMember = memberUtil.getCurrentMember();
        return MemberInfoResponse.from(currentMember);
    }

    public MemberDashboardResponse getDashboard() {
        final Member member = memberUtil.getCurrentMember();
        final Optional<UnivEmailVerification> univEmailVerification =
                univEmailVerificationService.getUnivEmailVerificationFromRedis(member.getId());
        UnivVerificationStatus univVerificationStatus =
                emailVerificationStatusService.determineStatus(member, univEmailVerification);
        Optional<RecruitmentRound> currentRecruitmentRound = onboardingRecruitmentService.findCurrentRecruitmentRound();
        Optional<Membership> myMembership = currentRecruitmentRound.flatMap(
                recruitmentRound -> membershipService.findMyMembership(member, recruitmentRound));

        return MemberDashboardResponse.of(
                member, univVerificationStatus, currentRecruitmentRound.orElse(null), myMembership.orElse(null));
    }

    public MemberPreviousInfoResponse getPreviousInfoByStudentId(String studentId) {
        Member previousMember =
                memberRepository.findByStudentId(studentId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        String previousGithubHandle = githubClient.getGithubHandle(previousMember.getOauthId());
        String previousEmail = previousMember.getEmail();

        return MemberPreviousInfoResponse.of(previousMember.getId(), previousGithubHandle, previousEmail);
    }

    @Transactional
    public void attemptAdvanceToAssociate(long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.advanceToAssociate();

        memberRepository.save(member);

        log.info("[OnboardingMemberService] 준회원 승급 완료: memberId={}", member.getId());
    }

    public MemberStudentIdDuplicateResponse checkStudentIdDuplicate(String studentId) {
        boolean isStudentIdDuplicate = memberRepository.existsByStudentId(studentId);
        return MemberStudentIdDuplicateResponse.from(isStudentIdDuplicate);
    }
}
