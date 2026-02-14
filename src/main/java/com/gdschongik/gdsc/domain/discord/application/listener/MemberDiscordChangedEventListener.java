package com.gdschongik.gdsc.domain.discord.application.listener;

import com.gdschongik.gdsc.domain.discord.application.handler.MemberDiscordRoleRevokeHandler;
import com.gdschongik.gdsc.domain.member.domain.event.MemberDiscordChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDiscordChangedEventListener {

    private final MemberDiscordRoleRevokeHandler memberDiscordRoleRevokeHandler;

    @ApplicationModuleListener
    public void changeMemberDiscord(MemberDiscordChangedEvent event) {
        log.info(
                "[MemberDiscordChangedEventListener] 회원 디스코드 변경 이벤트 수신: memberId={}, previousDiscordId={}, currentDiscordId={}",
                event.memberId(),
                event.previousDiscordId(),
                event.currentDiscordId());
        memberDiscordRoleRevokeHandler.delegate(event);
    }
}
