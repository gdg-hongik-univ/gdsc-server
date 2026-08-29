package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.dao.UnivEmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.dao.VerificationAttemptCounter;
import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO SendCodeService와 통합 검토
public class UnivEmailVerificationService {

    private final MemberRepository memberRepository;
    private final UnivEmailVerificationRepository univEmailVerificationRepository;
    private final VerificationAttemptCounter verificationAttemptCounter;
    private final MemberUtil memberUtil;
    private final UnivEmailValidator univEmailValidator;

    @Transactional
    public void verifyMemberUnivEmail(UnivEmailVerificationRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        UnivEmailVerification univEmailVerification = univEmailVerificationRepository
                .findById(currentMember.getId())
                .orElseThrow(() -> new CustomException(UNIV_EMAIL_VERIFICATION_CODE_NOT_SENT));
        validateCode(univEmailVerification, request.code(), currentMember.getId());

        // TODO: 어플리케이션 이벤트 발행 방식으로 변경
        currentMember.completeUnivEmailVerification(univEmailVerification.getUnivEmail());
        memberRepository.save(currentMember);
        univEmailVerificationRepository.delete(univEmailVerification);

        log.info("[UnivEmailVerificationService] 재학생 인증 완료: memberId={}", currentMember.getId());
    }

    public Optional<UnivEmailVerification> getUnivEmailVerificationFromRedis(Long memberId) {
        return univEmailVerificationRepository.findById(memberId);
    }

    /**
     * 시도 횟수를 증가시킨 뒤 인증 코드를 검증하고, 시도 횟수를 초과한 경우 인증 정보를 삭제하여 무효화합니다.
     */
    private void validateCode(UnivEmailVerification univEmailVerification, String code, Long memberId) {
        long attemptCount = verificationAttemptCounter.increaseUnivEmailVerificationAttemptCount(memberId);

        try {
            univEmailValidator.validateUnivEmailVerificationCode(univEmailVerification, code, attemptCount);
        } catch (CustomException e) {
            if (e.getErrorCode() == EMAIL_VERIFICATION_CODE_ATTEMPT_EXCEEDED) {
                univEmailVerificationRepository.delete(univEmailVerification);
                log.warn(
                        "[UnivEmailVerificationService] 재학생 인증 시도 횟수 초과: memberId={}, attemptCount={}",
                        memberId,
                        attemptCount);
            }
            throw e;
        }
    }
}
