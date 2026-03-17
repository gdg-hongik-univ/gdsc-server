package com.gdschongik.gdsc.domain.email.application;

import com.gdschongik.gdsc.domain.email.dao.UnivEmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationRequest;
import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationTokenDto;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import com.gdschongik.gdsc.global.util.email.EmailVerificationTokenUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UnivEmailVerificationService {

    private final EmailVerificationTokenUtil emailVerificationTokenUtil;
    private final MemberRepository memberRepository;
    private final UnivEmailVerificationRepository univEmailVerificationRepository;
    private final UnivEmailValidator univEmailValidator;

    @Transactional
    public void verifyMemberUnivEmail(UnivEmailVerificationRequest request) {
        UnivEmailVerificationTokenDto emailVerificationToken = getEmailVerificationToken(request.token());
        Member member = getMemberById(emailVerificationToken.memberId());
        member.completeUnivEmailVerification(emailVerificationToken.email());
        memberRepository.save(member);
    }

    public Optional<UnivEmailVerification> getUnivEmailVerificationFromRedis(Long memberId) {
        return univEmailVerificationRepository.findById(memberId);
    }

    private UnivEmailVerificationTokenDto getEmailVerificationToken(String verificationToken) {
        UnivEmailVerificationTokenDto univEmailVerificationTokenDto =
                emailVerificationTokenUtil.parseEmailVerificationTokenDto(verificationToken);
        final Optional<UnivEmailVerification> univEmailVerification =
                getUnivEmailVerificationFromRedis(univEmailVerificationTokenDto.memberId());

        univEmailValidator.validateUnivEmailVerification(univEmailVerification, verificationToken);

        return univEmailVerificationTokenDto;
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
