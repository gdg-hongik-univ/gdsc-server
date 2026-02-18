package com.gdschongik.gdsc.domain.email.application;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.domain.email.dto.request.EmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import com.gdschongik.gdsc.global.security.MemberAuthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailVerificationService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    // TODO: 이메일 인증, oauthId 변경, 임시 멤버 삭제, 토큰 재발급 등 4가지 책임이 하나의 흐름에 결합되어 있음.
    //  도메인 이벤트를 활용하여 책임을 분리하는 리팩토링 필요.
    @Transactional
    public MemberAuthInfo verifyMemberEmail(EmailVerificationRequest request) {
        EmailVerification emailVerification = emailVerificationRepository
                .findById(request.token())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));
        Member currentMember = memberRepository
                .findById(emailVerification.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member previousMember = memberRepository
                .findById(emailVerification.getPreviousMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        updatePreviousMemberOauthId(previousMember, currentMember);
        deleteCurrentMember(currentMember);

        return MemberAuthInfo.from(previousMember);
    }

    private void updatePreviousMemberOauthId(Member previousMember, Member currentMember) {
        previousMember.updateOauthId(currentMember.getOauthId());
        memberRepository.save(previousMember);
        log.info(
                "[EmailVerificationService] 이메일 인증 완료: memberId={}, email={}",
                previousMember.getId(),
                previousMember.getEmail());
    }

    private void deleteCurrentMember(Member currentMember) {
        currentMember.withdraw();
        memberRepository.save(currentMember);
        log.info(
                "[EmailVerificationService] 임시 회원 탈퇴 처리 완료: memberId={}, email={}",
                currentMember.getId(),
                currentMember.getEmail());
    }
}
