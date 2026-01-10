# DISCORD.md

이 문서는 gdsc-server의 Discord 봇 통합 아키텍처와 개발 가이드를 정의합니다.

---

## 1. 개요

gdsc-server는 JDA(Java Discord API)를 사용하여 Discord 봇 기능을 제공합니다. 주요 기능은 다음과 같습니다:

- **회원 인증**: Discord 연동을 위한 인증코드 발급 및 검증
- **역할 관리**: 정회원/스터디 역할 자동 부여 및 제거
- **공지 전송**: 스터디 공지를 Discord 채널로 자동 전송
- **어드민 기능**: 배치 작업, 권한 부여 등 관리 기능

---

## 2. 아키텍처 개요

### 2.1 핵심 컴포넌트

| 컴포넌트 | 역할 |
|---------|------|
| `DiscordConfig` | JDA 초기화, Slash Command 등록, 빈 구성 |
| `DiscordUtil` | Discord 관련 공통 유틸리티 (역할/멤버 조회, 메시지 전송) |
| `Listener` | JDA 이벤트 수신 및 Handler로 위임 |
| `Handler` | 비즈니스 로직 처리 |
| `Aspect` | AOP 기반 예외 처리 |
| `Service` | 도메인 로직 처리 |

### 2.2 JDA 이벤트 처리 흐름

```
[Discord Server]
    |
    v (JDA Event)
[JDA]
    |
    v (Event Dispatch)
[@Listener Listener]
    |
    v (커맨드명 매칭)
[DiscordEventHandler.delegate()]
    |
    v (AOP 적용)
[DiscordEventHandlerAspect] -- 예외 발생 --> [DiscordExceptionDispatcher] --> [ExceptionHandler]
    |
    v (정상 처리)
[Service Layer]
    |
    v
[Repository / DiscordUtil]
```

### 2.3 Spring 이벤트 -> Discord 알림 흐름

```
[Domain Logic]
    |
    v (registerEvent)
[Spring Modulith Event]
    |
    v (@ApplicationModuleListener)
[Discord Event Listener]
    |
    v
[SpringEventHandler.delegate()]
    |
    v (AOP 적용)
[SpringEventHandlerAspect] -- 예외 발생 --> [Admin Channel 알림]
    |
    v (정상 처리)
[DiscordUtil]
    |
    v
[Discord Server]
```

---

## 3. 설정 및 구성

### 3.1 환경 변수

**파일**: `src/main/resources/application-discord.yml`

```yaml
discord:
  token: ${DISCORD_BOT_TOKEN:}
  server-id: ${DISCORD_SERVER_ID:}
  command-channel-id: ${DISCORD_COMMAND_CHANNEL_ID:}
  admin-channel-id: ${DISCORD_ADMIN_CHANNEL_ID:}
```

| 환경 변수 | 설명 |
|----------|------|
| `DISCORD_BOT_TOKEN` | Discord 봇 토큰 |
| `DISCORD_SERVER_ID` | 서버(길드) ID |
| `DISCORD_COMMAND_CHANNEL_ID` | 커맨드 채널 ID |
| `DISCORD_ADMIN_CHANNEL_ID` | 어드민 채널 ID (예외 알림용) |

### 3.2 JDA 구성

**파일**: `src/main/java/com/gdschongik/gdsc/global/config/DiscordConfig.java`

```java
JDA jda = JDABuilder.createDefault(discordProperty.getToken())
        .setActivity(Activity.playing(DISCORD_BOT_STATUS_CONTENT))
        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .setChunkingFilter(ChunkingFilter.ALL)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .build();
```

**구성 요소**:
- **Gateway Intents**: `GUILD_MESSAGES`, `MESSAGE_CONTENT`, `GUILD_MEMBERS`
- **Chunking Filter**: `ALL` (모든 멤버 로드)
- **Member Cache Policy**: `ALL` (모든 멤버 캐시)
- **Activity**: "정상영업" 상태 표시

### 3.3 조건부 빈 생성

```java
@ConditionalOnProperty(value = "discord.enabled", havingValue = "true", matchIfMissing = true)
```

- `discord.enabled: false` 설정 시 Discord 기능 비활성화
- 테스트 환경에서 `application-test.yml`에 `discord.enabled: false` 설정

### 3.4 DiscordUtil 빈 등록

```java
@Bean
@ConditionalOnBean(JDA.class)
public DiscordUtil discordUtil(JDA jda, DiscordProperty discordProperty) {
    return new DiscordUtil(jda, discordProperty);
}

@Bean
@Order(1)
public DiscordUtil fallbackDiscordUtil() {
    return new DiscordUtil(null, null);
}
```

- JDA 빈이 있으면 정상 `DiscordUtil` 생성
- JDA 빈이 없으면 fallback `DiscordUtil` 생성 (테스트 환경용)

---

## 4. 이벤트 핸들링

### 4.1 @Listener 어노테이션

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/application/listener/Listener.java`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Listener {}
```

`@Listener` 어노테이션이 붙은 클래스는:
1. `@Component`로 Spring 빈 등록
2. `ListenerBeanPostProcessor`가 JDA 이벤트 리스너로 자동 등록

> **주의**: `@Listener`가 이미 `@Component`를 포함하므로, `@Component`를 중복 사용하지 마세요.

### 4.2 ListenerBeanPostProcessor

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/application/listener/ListenerBeanPostProcessor.java`

```java
@RequiredArgsConstructor
public class ListenerBeanPostProcessor implements BeanPostProcessor {

    private final JDA jda;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean.getClass().isAnnotationPresent(Listener.class)) {
            jda.addEventListener(bean);
        }
        return bean;
    }
}
```

### 4.3 Listener-Handler 패턴

모든 JDA 이벤트는 **Listener-Handler 패턴**으로 처리됩니다:

- **Listener**: 이벤트 수신 및 커맨드명 매칭
- **Handler**: 비즈니스 로직 처리

**Listener 예시**:

```java
@Listener
@RequiredArgsConstructor
public class IssuingCodeCommandListener extends ListenerAdapter {

    private final IssuingCodeCommandHandler issuingCodeCommandHandler;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(COMMAND_NAME_ISSUING_CODE)) {
            issuingCodeCommandHandler.delegate(event);
        }
    }
}
```

**Handler 예시**:

```java
@Component
@RequiredArgsConstructor
public class IssuingCodeCommandHandler implements DiscordEventHandler {

    private final OnboardingDiscordService onboardingDiscordService;

    @Override
    public void delegate(GenericEvent genericEvent) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) genericEvent;

        event.deferReply()
                .setEphemeral(true)
                .setContent(DEFER_MESSAGE_ISSUING_CODE)
                .queue();

        String discordUsername = event.getUser().getName();
        DiscordVerificationCodeResponse verificationCode =
                onboardingDiscordService.createVerificationCode(discordUsername);

        String message = REPLY_MESSAGE_ISSUING_CODE.formatted(verificationCode.code());
        event.getHook().sendMessage(message).setEphemeral(true).queue();
    }
}
```

### 4.4 Handler 인터페이스

**JDA 이벤트용**: `DiscordEventHandler`

```java
public interface DiscordEventHandler {
    void delegate(GenericEvent genericEvent);
}
```

**Spring 이벤트용**: `SpringEventHandler`

```java
public interface SpringEventHandler {
    void delegate(Object context);
}
```

### 4.5 JDA 이벤트 리스너 목록

| 리스너 클래스 | 이벤트 타입 | 역할 |
|--------------|------------|------|
| `IssuingCodeCommandListener` | `SlashCommandInteractionEvent` | 인증코드 발급 |
| `DiscordIdBatchCommandListener` | `SlashCommandInteractionEvent` | Discord ID 배치 저장 |
| `AssignAdminRoleCommandListener` | `SlashCommandInteractionEvent` | 어드민 권한 부여 |
| `AssignStudyRoleCommandListener` | `SlashCommandInteractionEvent` | 스터디 역할 부여 |
| `AdvanceFailedMemberCommandListener` | `SlashCommandInteractionEvent` | 정회원 승급 누락자 처리 |
| `NonCommandListener` | `MessageReceivedEvent` | 비커맨드 메시지 삭제 |
| `NicknameModifyListener` | `GuildMemberUpdateNicknameEvent` | 닉네임 변경 복원 |
| `PingpongListener` | `MessageReceivedEvent` | 개발용 핑퐁 (LOCAL/DEV만) |

---

## 5. Slash Command

### 5.1 커맨드 등록

**파일**: `src/main/java/com/gdschongik/gdsc/global/config/DiscordConfig.java`

```java
Objects.requireNonNull(jda.awaitReady().getGuildById(discordProperty.getServerId()))
        .updateCommands()
        .addCommands(Commands.slash(COMMAND_NAME_ISSUING_CODE, COMMAND_DESCRIPTION_ISSUING_CODE))
        .addCommands(Commands.slash(COMMAND_NAME_BATCH_DISCORD_ID, COMMAND_DESCRIPTION_BATCH_DISCORD_ID))
        // ... 추가 커맨드
        .queue();
```

### 5.2 등록된 커맨드 목록

| 커맨드명 | 설명 | 옵션 |
|---------|------|------|
| `인증코드` | 디스코드 연동 인증코드 발급 | 없음 |
| `디스코드id-저장하기` | 배치 작업으로 Discord ID 저장 | 없음 |
| `정회원-승급-누락자-승급하기` | 정회원 승급 누락자 처리 | 없음 |
| `어드민-권한-부여` | 특정 멤버에게 어드민 권한 부여 | `학번` (STRING, required) |
| `스터디-역할-부여` | 스터디 역할 부여 | `스터디-이름`, `스터디-연도`, `스터디-학기` |

### 5.3 커맨드 처리 흐름

1. **이벤트 수신**: Listener가 `SlashCommandInteractionEvent` 수신
2. **커맨드명 매칭**: `event.getName().equals(COMMAND_NAME_XXX)`
3. **핸들러 위임**: `handler.delegate(event)`
4. **deferReply 호출**: 처리 중임을 알림 (ephemeral)
5. **비즈니스 로직 수행**: 서비스 호출
6. **응답 전송**: `event.getHook().sendMessage()` (ephemeral)

### 5.4 ephemeral 응답 패턴

모든 커맨드가 ephemeral 응답을 사용하여 **본인에게만** 메시지가 표시됩니다:

```java
// deferReply
event.deferReply(true)
    .setContent(DEFER_MESSAGE_XXX)
    .queue();

// 비즈니스 로직 수행

// 응답 전송
event.getHook()
    .sendMessage(REPLY_MESSAGE_XXX)
    .setEphemeral(true)
    .queue();
```

### 5.5 상수 관리

**파일**: `src/main/java/com/gdschongik/gdsc/global/common/constant/DiscordConstant.java`

| 접두어 | 용도 | 예시 |
|-------|------|------|
| `COMMAND_NAME_` | 커맨드 이름 | `COMMAND_NAME_ISSUING_CODE = "인증코드"` |
| `COMMAND_DESCRIPTION_` | 커맨드 설명 | `COMMAND_DESCRIPTION_ISSUING_CODE = "..."` |
| `DEFER_MESSAGE_` | 처리 중 메시지 | `DEFER_MESSAGE_ISSUING_CODE = "..."` |
| `REPLY_MESSAGE_` | 완료 메시지 | `REPLY_MESSAGE_ISSUING_CODE = "..."` |
| `OPTION_NAME_` | 옵션 이름 | `OPTION_NAME_ASSIGN_ADMIN_ROLE = "학번"` |
| `OPTION_DESCRIPTION_` | 옵션 설명 | `OPTION_DESCRIPTION_ASSIGN_ADMIN_ROLE = "..."` |

---

## 6. Spring 이벤트 통합

### 6.1 @ApplicationModuleListener 사용

Spring Modulith의 `@ApplicationModuleListener`를 사용하여 도메인 이벤트를 Discord 알림으로 연동합니다.

```java
@Component
@RequiredArgsConstructor
public class DelegateMemberDiscordEventListener {

    private final DelegateMemberDiscordEventHandler delegateMemberDiscordEventHandler;

    @ApplicationModuleListener
    public void delegateMemberDiscordEvent(MemberAdvancedToRegularEvent event) {
        log.info("[DelegateMemberDiscordEventListener] 정회원 승급 이벤트 수신: memberId={}", event.memberId());
        delegateMemberDiscordEventHandler.delegate(event);
    }
}
```

> **주의**: Spring 이벤트 리스너는 `@Listener` 어노테이션을 사용하지 않습니다. `@Listener`는 JDA 리스너 전용입니다.

### 6.2 이벤트-핸들러 매핑

| 도메인 이벤트 | 리스너 | 핸들러 | 동작 |
|--------------|--------|--------|------|
| `MemberAdvancedToRegularEvent` | `DelegateMemberDiscordEventListener` | `DelegateMemberDiscordEventHandler` | 정회원 역할 부여 |
| `MemberDemotedToAssociateEvent` | `MemberDemotedToAssociateEventListener` | `MemberDiscordRoleRevokeHandler` | 정회원 역할 제거 |

### 6.3 핸들러 구현

```java
@Component
@RequiredArgsConstructor
public class DelegateMemberDiscordEventHandler implements SpringEventHandler {

    private final DiscordUtil discordUtil;

    @Override
    public void delegate(Object context) {
        MemberAdvancedToRegularEvent event = (MemberAdvancedToRegularEvent) context;
        Guild guild = discordUtil.getCurrentGuild();
        Member member = discordUtil.getMemberById(event.discordId());
        Role role = discordUtil.findRoleByName(MEMBER_ROLE_NAME);

        guild.addRoleToMember(member, role).queue();
    }
}
```

---

## 7. 예외 처리

### 7.1 AOP 기반 예외 처리

Discord 이벤트 처리 중 발생하는 예외는 AOP Aspect가 자동으로 처리합니다.

**JDA 이벤트용**: `DiscordEventHandlerAspect`

```java
@Aspect
@Component
@RequiredArgsConstructor
public class DiscordEventHandlerAspect {

    private final DiscordExceptionDispatcher discordExceptionDispatcher;

    @Around("execution(* ...DiscordEventHandler.delegate(*)) && args(genericEvent)")
    public Object doAround(ProceedingJoinPoint joinPoint, GenericEvent genericEvent) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            discordExceptionDispatcher.dispatch(e, genericEvent);
            return null;
        }
    }
}
```

**Spring 이벤트용**: `SpringEventHandlerAspect`

```java
@Aspect
@Component
@RequiredArgsConstructor
public class SpringEventHandlerAspect {

    private final DiscordUtil discordUtil;

    @Around("execution(* ...SpringEventHandler.delegate(*)) && args(ignoredContext)")
    public Object doAround(ProceedingJoinPoint joinPoint, Object ignoredContext) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("[SpringEventHandlerAspect] Exception occurred", e);
            sendErrorMessageToDiscord(e);
            return null;
        }
    }

    private void sendErrorMessageToDiscord(Exception e) {
        TextChannel channel = discordUtil.getAdminChannel();
        channel.sendMessage(e.getMessage()).queue();
    }
}
```

### 7.2 예외 디스패처

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/exception/DiscordExceptionDispatcher.java`

```java
@Component
public class DiscordExceptionDispatcher {

    private static final Map<Class<? extends GenericEvent>, DiscordExceptionHandler> exceptionHandlerMap =
            Map.of(SlashCommandInteractionEvent.class, new CommandExceptionHandler());

    private static final DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();

    public void dispatch(Exception exception, Object context) {
        log.error("DiscordException: {}", exception.getMessage());
        DiscordExceptionHandler exceptionHandler =
                exceptionHandlerMap.getOrDefault(context.getClass(), defaultExceptionHandler);
        exceptionHandler.handle(exception, context);
    }
}
```

### 7.3 예외 핸들러

**CommandExceptionHandler**: Slash Command 예외 처리

```java
public class CommandExceptionHandler implements DiscordExceptionHandler {

    @Override
    public void handle(Exception exception, Object context) {
        GenericCommandInteractionEvent event = (GenericCommandInteractionEvent) context;
        String message = DiscordExceptionMessageGenerator.generate(exception);
        event.getHook().sendMessage(message).setEphemeral(true).queue();
    }
}
```

**DefaultExceptionHandler**: 기본 예외 핸들러 (no-op)

```java
public class DefaultExceptionHandler implements DiscordExceptionHandler {

    @Override
    public void handle(Exception exception, Object context) {
        // do nothing
    }
}
```

### 7.4 예외 메시지 생성

```java
public class DiscordExceptionMessageGenerator {

    public static String generate(Exception exception) {
        if (exception instanceof CustomException) {
            return exception.getMessage();
        }
        return "알 수 없는 오류가 발생했습니다.";
    }
}
```

> **핸들러에서 try-catch 불필요**: AOP가 예외를 자동 처리하므로, Handler 내부에서 직접 try-catch를 사용하지 마세요.

---

## 8. 서비스 계층

### 8.1 CommonDiscordService

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/application/CommonDiscordService.java`

| 메서드 | 설명 |
|--------|------|
| `getNicknameByDiscordUsername(String)` | 디스코드 유저명으로 닉네임 조회 |
| `batchDiscordId(String, RequirementStatus)` | 배치 작업으로 Discord ID 저장 |
| `assignDiscordStudyRole(String, String, Integer, String)` | 스터디 역할 일괄 부여 |
| `addStudyRoleToMember(String, String)` | 특정 멤버에게 스터디 역할 부여 |
| `removeStudyRoleFromMember(String, String)` | 특정 멤버에서 스터디 역할 제거 |
| `sendStudyAnnouncement(Long)` | 스터디 공지 Discord 채널로 전송 |

### 8.2 OnboardingDiscordService

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/application/OnboardingDiscordService.java`

| 메서드 | 설명 |
|--------|------|
| `createVerificationCode(String)` | 인증코드 생성 (TTL 5분) |
| `verifyDiscordCode(DiscordLinkRequest)` | 인증코드 검증 및 연동 완료 |
| `checkUsernameDuplicate(String)` | 디스코드 유저명 중복 확인 |
| `checkNicknameDuplicate(String)` | 닉네임 중복 확인 |
| `checkServerJoined(String)` | 디스코드 서버 합류 여부 확인 |

---

## 9. 도메인 모델

### 9.1 DiscordVerificationCode (인증 코드)

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/domain/DiscordVerificationCode.java`

```java
@Getter
@RedisHash("discordVerificationCode")
public class DiscordVerificationCode {

    public static final int MIN_CODE_RANGE = 1000;
    public static final int MAX_CODE_RANGE = 9999;

    @Id
    private String discordUsername;

    private Integer code;

    @TimeToLive
    private Long ttl;

    public static DiscordVerificationCode create(String discordUsername, Integer code, Long ttl) {
        return DiscordVerificationCode.builder()
                .discordUsername(discordUsername)
                .code(code)
                .ttl(ttl)
                .build();
    }

    public boolean matchesCode(Integer code) {
        return this.code.equals(code);
    }
}
```

**특징**:
- `@RedisHash`: Redis에 저장
- `@TimeToLive`: TTL 자동 관리 (300초 = 5분)
- 4자리 숫자 코드 (1000-9999)
- `discordUsername`을 ID로 사용

### 9.2 DiscordValidator

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/domain/service/DiscordValidator.java`

```java
@DomainService
public class DiscordValidator {

    public void validateVerifyDiscordCode(
            Integer requestedCode,
            DiscordVerificationCode discordVerificationCode,
            boolean isDiscordUsernameDuplicate,
            boolean isNicknameDuplicate) {
        // 코드 일치, 유저명 중복, 닉네임 중복 검증
    }

    public void validateAdminPermission(Member currentMember) {
        // 어드민 권한 검증
    }
}
```

---

## 10. REST API

### 10.1 엔드포인트

**파일**: `src/main/java/com/gdschongik/gdsc/domain/discord/api/OnboardingDiscordController.java`

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `/onboarding/me/link-discord` | POST | 디스코드 연동 (인증코드 검증) |
| `/onboarding/check-discord-username` | GET | 유저명 중복 확인 |
| `/onboarding/check-discord-nickname` | GET | 닉네임 중복 확인 |
| `/onboarding/check-discord-join` | GET | 서버 합류 확인 |

### 10.2 Request/Response DTO

**DiscordLinkRequest**:

```java
public record DiscordLinkRequest(
        @NotBlank String discordUsername,
        @NotBlank @Pattern(regexp = NICKNAME) String nickname,
        @Range(min = 1000, max = 9999) Integer code) {}
```

**DiscordVerificationCodeResponse**:

```java
public record DiscordVerificationCodeResponse(Integer code, Duration ttl) {}
```

**DiscordCheckDuplicateResponse**:

```java
public record DiscordCheckDuplicateResponse(Boolean isDuplicate) {}
```

**DiscordCheckJoinResponse**:

```java
public record DiscordCheckJoinResponse(boolean isJoined) {}
```

---

## 11. DiscordUtil 유틸리티

### 11.1 역할(Role) 조회

```java
public Role findRoleByName(String roleName);  // 이름으로 조회 (대소문자 무시)
public Role findRoleById(String roleId);       // ID로 조회
```

### 11.2 길드/채널 조회

```java
public Guild getCurrentGuild();      // 현재 길드 조회
public TextChannel getAdminChannel(); // 어드민 채널 조회
```

### 11.3 멤버 조회

```java
public Optional<Member> getOptionalMemberByUsername(String username);
public Member getMemberById(String discordId);
public String getMemberIdByUsername(String username);
```

### 11.4 역할 관리

```java
public void addRoleToMemberById(String discordRoleId, String memberDiscordId);
public void removeRoleFromMemberById(String discordRoleId, String memberDiscordId);
```

### 11.5 공지 전송

```java
public void sendStudyAnnouncementToChannel(
        String channelId,
        String discordRoleId,
        String studyName,
        String title,
        String link,
        LocalDateTime createdAt);
```

- Embed 메시지로 전송
- 스터디 역할 멘션 포함
- 동적 이미지 생성 URL 사용: `https://image.wawoo.dev/api/v1/study-announcement`
- KST -> UTC 타임존 변환

---

## 12. 개발 가이드

### 12.1 새로운 Slash Command 추가

1. **상수 정의** (`DiscordConstant.java`):

```java
public static final String COMMAND_NAME_NEW_COMMAND = "새커맨드";
public static final String COMMAND_DESCRIPTION_NEW_COMMAND = "새 커맨드 설명";
public static final String DEFER_MESSAGE_NEW_COMMAND = "처리 중입니다...";
public static final String REPLY_MESSAGE_NEW_COMMAND = "처리 완료!";
```

2. **커맨드 등록** (`DiscordConfig.java`):

```java
.addCommands(Commands.slash(COMMAND_NAME_NEW_COMMAND, COMMAND_DESCRIPTION_NEW_COMMAND))
```

3. **Handler 생성**:

```java
@Component
@RequiredArgsConstructor
public class NewCommandHandler implements DiscordEventHandler {

    private final SomeService someService;

    @Override
    public void delegate(GenericEvent genericEvent) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) genericEvent;

        event.deferReply(true)
            .setContent(DEFER_MESSAGE_NEW_COMMAND)
            .queue();

        // 비즈니스 로직

        event.getHook()
            .sendMessage(REPLY_MESSAGE_NEW_COMMAND)
            .setEphemeral(true)
            .queue();
    }
}
```

4. **Listener 생성**:

```java
@Listener  // @Component 중복 사용 금지
@RequiredArgsConstructor
public class NewCommandListener extends ListenerAdapter {

    private final NewCommandHandler newCommandHandler;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(COMMAND_NAME_NEW_COMMAND)) {
            newCommandHandler.delegate(event);
        }
    }
}
```

### 12.2 Spring 이벤트 기반 Discord 알림 추가

1. **Handler 생성**:

```java
@Component
@RequiredArgsConstructor
public class NewEventDiscordHandler implements SpringEventHandler {

    private final DiscordUtil discordUtil;

    @Override
    public void delegate(Object context) {
        NewDomainEvent event = (NewDomainEvent) context;
        // Discord 알림 로직
    }
}
```

2. **Listener 생성** (`@Listener` 사용 금지):

```java
@Component
@RequiredArgsConstructor
public class NewEventDiscordListener {

    private final NewEventDiscordHandler handler;

    @ApplicationModuleListener
    public void handleNewEvent(NewDomainEvent event) {
        handler.delegate(event);
    }
}
```

### 12.3 주의사항

1. **@Listener 어노테이션**: JDA 리스너에만 사용. `@Component` 중복 금지
2. **Handler 예외 처리**: AOP가 자동 처리하므로 try-catch 불필요
3. **ephemeral 응답**: 모든 Slash Command는 ephemeral 응답 사용
4. **상수 관리**: 모든 커맨드 관련 문자열은 `DiscordConstant`에 정의
5. **Spring 이벤트 리스너**: `@ApplicationModuleListener` 사용, `@Listener` 사용 금지

---

## 13. 개선 권장 사항

### 13.1 ReadyListener 방식 변경 필요

**파일**: `DiscordConfig.java:42`

현재 `jda.awaitReady()` 후 리스너 빈이 초기화되면 이벤트 수신이 불가능할 수 있습니다. `@PostConstruct`를 사용한 ReadyListener 방식으로 변경을 권장합니다.

### 13.2 instanceof 패턴 매칭 적용

**파일**: `DiscordExceptionDispatcher.java:18`

현재 Map 기반 핸들러 매핑을 instanceof 패턴 매칭으로 대체하면 가독성이 향상됩니다.

### 13.3 @Component 중복 제거 필요

다음 리스너들에서 `@Component` 어노테이션 제거 필요:
- `AssignAdminRoleCommandListener`
- `AdvanceFailedMemberCommandListener`
- `DiscordIdBatchCommandListener`
- `AssignStudyRoleCommandListener`

### 13.4 @Override 어노테이션 추가 필요

위 리스너들의 `onSlashCommandInteraction` 메서드에 `@Override` 어노테이션 추가 필요.
