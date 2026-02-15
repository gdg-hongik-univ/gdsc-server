package com.gdschongik.gdsc.domain.email.application;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.domain.email.dto.request.EmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailVerificationService {

    private final MemberUtil memberUtil;
    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UnivEmailValidator univEmailValidator;

    @Transactional
    public void verifyMemberEmail(EmailVerificationRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        EmailVerification emailVerification = getEmailVerification(currentMember.getId(), request.token());
        Member previousMember = memberRepository
                .findById(emailVerification.getPreviousMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        updatePreviousMemberOauthId(previousMember, currentMember);
        deleteCurrentMember(currentMember);
    }

    private EmailVerification getEmailVerification(Long memberId, String verificationToken) {
        Optional<EmailVerification> emailVerification = emailVerificationRepository.findById(memberId);
        univEmailValidator.validateEmailVerification(emailVerification, verificationToken);
        return emailVerification.get();
    }

    private void updatePreviousMemberOauthId(Member previousMember, Member currentMember) {
        previousMember.updateOauthId(currentMember.getOauthId());
        memberRepository.save(previousMember);
        log.info("이메일 인증 완료: memberId={}, email={}", previousMember.getId(), previousMember.getEmail());
    }

    private void deleteCurrentMember(Member currentMember) {
        currentMember.withdraw();
        memberRepository.save(currentMember);
        log.info("임시 회원 탈퇴 처리 완료: memberId={}, email={}", currentMember.getId(), currentMember.getEmail());
    }
}
