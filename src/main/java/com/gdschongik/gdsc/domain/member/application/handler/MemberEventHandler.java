package com.gdschongik.gdsc.domain.member.application.handler;

import com.gdschongik.gdsc.domain.email.domain.event.PreviousEmailVerifiedEvent;
import com.gdschongik.gdsc.domain.member.application.OnboardingMemberService;
import com.gdschongik.gdsc.domain.member.domain.event.MemberAssociateRequirementUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventHandler {

    private final OnboardingMemberService onboardingMemberService;

    @ApplicationModuleListener
    public void handleMemberAssociateRequirementUpdatedEvent(MemberAssociateRequirementUpdatedEvent event) {
        log.info("[MemberEventHandler] 멤버 준회원 승급조건 업데이트 이벤트 수신: memberId={}", event.memberId());

        onboardingMemberService.attemptAdvanceToAssociate(event.memberId());
    }

    @ApplicationModuleListener
    public void handlePreviousEmailVerifiedEvent(PreviousEmailVerifiedEvent event) {
        log.info(
                "[EmailEventHandler] 과거 계정 이메일 인증 완료 이벤트 수신: currentMemberId={}, previousMemberId={}",
                event.currentMemberId(),
                event.previousMemberId());

        onboardingMemberService.changeOauthId(event.currentMemberId(), event.previousMemberId());
    }
}
