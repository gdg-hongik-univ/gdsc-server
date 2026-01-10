# EXTERNAL.md

이 문서는 gdsc-server의 외부 서비스 연동 아키텍처를 설명합니다.

---

## 개요

gdsc-server는 다음 외부 서비스와 연동합니다:

| 서비스 | 용도 | 연동 방식 |
|--------|------|----------|
| GitHub | 과제 제출 조회, 레포지토리 관리 | kohsuke/github-api |
| 토스페이먼츠 | 결제 승인/조회/취소 | Feign Client |
| Gmail | 이메일 인증 발송 | SMTP (JavaMailSender) |
| Sentry | 에러 모니터링, 분산 추적 | sentry-spring-boot-starter |

---

## 1. Feign 클라이언트 전역 설정

### 1.1 설정 클래스

**파일**: `infra/feign/global/config/FeignConfig.java`

```java
@Configuration
@EnableFeignClients("com.gdschongik.gdsc.infra")
public class FeignConfig {

    @Bean
    public Decoder feignDecoder() {
        return new JacksonDecoder(customObjectMapper());
    }

    public ObjectMapper customObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public FeignFormatterRegistrar dateTimeFormatterRegistrar() {
        return registry -> {
            var registrar = new DateTimeFormatterRegistrar();
            registrar.setUseIsoFormat(true);
            registrar.registerFormatters(registry);
        };
    }

    @Bean
    public Capability sentryCapability() {
        return new SentryCapability();
    }
}
```

### 1.2 주요 설정

| 설정 | 값 | 설명 |
|------|------|------|
| 스캔 범위 | `com.gdschongik.gdsc.infra` | 해당 패키지 하위 `@FeignClient` 자동 등록 |
| `FAIL_ON_UNKNOWN_PROPERTIES` | `false` | 알 수 없는 JSON 속성 무시 (API 버전 변경 대응) |
| `READ_UNKNOWN_ENUM_VALUES_AS_NULL` | `true` | 알 수 없는 enum 값을 null로 처리 |
| Logger Level | `FULL` | 헤더, 본문, 메타데이터 전체 로깅 |
| DateTime 형식 | ISO 8601 | 날짜/시간 파라미터 일관된 직렬화 |
| SentryCapability | 활성화 | 외부 API 호출 분산 추적 |

---

## 2. GitHub 연동

### 2.1 라이브러리 선택

**사용 라이브러리**: `org.kohsuke:github-api:1.323`

Feign 대신 kohsuke 라이브러리를 사용하는 이유:
- GitHub API의 복잡한 기능(커밋 조회, 파일 내용 읽기 등)을 위한 풍부한 API 제공
- 인증, 페이지네이션, 레이트 리밋 처리 내장
- 저수준 HTTP 호출보다 도메인 친화적인 API

### 2.2 Bean 설정

**파일**: `global/config/GithubConfig.java`

```java
@Configuration
@RequiredArgsConstructor
public class GithubConfig {

    private final GithubProperty githubProperty;

    @Bean
    public GitHub github() throws IOException {
        return new GitHubBuilder()
                .withOAuthToken(githubProperty.getSecretKey())
                .build();
    }
}
```

**설정 파일**: `application-github.yml`
```yaml
github:
  secret-key: ${GITHUB_SECRET_KEY:}
```

### 2.3 GithubClient 기능

**파일**: `infra/github/client/GithubClient.java`

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `getRepository(String repo)` | `GHRepository` | 레포지토리 정보 조회 |
| `getOwnerId(String repo)` | `String` | 레포지토리 소유자 ID 조회 |
| `getGithubHandle(String oauthId)` | `String` | OAuth ID → GitHub 핸들 변환 |
| `getOauthId(String githubHandle)` | `String` | GitHub 핸들 → OAuth ID 변환 |
| `getLatestAssignmentSubmissionFetcher(...)` | `AssignmentSubmissionFetcher` | 과제 제출 정보 조회 (지연 평가) |

### 2.4 커스텀 GitHubConnectorRequest

kohsuke 라이브러리가 지원하지 않는 API 호출을 위해 직접 구현:

**파일**: `infra/github/dto/request/`

| Request 클래스 | 용도 | API 엔드포인트 |
|---------------|------|----------------|
| `GithubUserByOauthIdRequest` | OAuth ID로 사용자 조회 | `https://api.github.com/user/{id}` |
| `GithubUserByHandleRequest` | 핸들로 사용자 조회 | `https://api.github.com/users/{handle}` |

```java
@RequiredArgsConstructor
public class GithubUserByOauthIdRequest implements GitHubConnectorRequest {

    private final String oauthId;

    @Override
    public String method() { return "GET"; }

    @Override
    public URL url() {
        return new URL(GITHUB_USER_BY_OAUTH_ID_API_URL.formatted(oauthId));
    }
    // ... 기타 인터페이스 메서드 구현
}
```

### 2.5 과제 제출 조회 - 지연 평가 패턴

GitHub API 호출 시 발생하는 예외를 과제 채점 로직에서 처리하기 위해 지연 평가 패턴을 적용합니다.

**구현 구조**:

```
AssignmentSubmissionFetcher (record)
├── repo: String
├── week: int
├── fetchExecutor: AssignmentSubmissionFetchExecutor
└── fetch(): AssignmentSubmission

AssignmentSubmissionFetchExecutor (@FunctionalInterface)
└── execute(String repo, int week): AssignmentSubmission

AssignmentSubmission (record)
├── url: String
├── commitHash: String
├── contentLength: Integer
└── committedAt: LocalDateTime
```

**사용 예시**:
```java
// GithubClient
public AssignmentSubmissionFetcher getLatestAssignmentSubmissionFetcher(String repo, int week) {
    return new AssignmentSubmissionFetcher(repo, week, this::getLatestAssignmentSubmission);
}

// 채점 로직에서 실제 호출
AssignmentSubmission submission = fetcher.fetch();  // 이 시점에 API 요청
```

### 2.6 과제 제출 조회 상세 로직

```java
private AssignmentSubmission getLatestAssignmentSubmission(String repo, int week) {
    GHRepository ghRepository = getRepository(repo);
    String assignmentPath = GITHUB_ASSIGNMENT_PATH.formatted(week);  // "week%d/wil.md"

    GHContent ghContent = getFileContent(ghRepository, assignmentPath);
    String content = readFileContent(ghContent);

    GHCommit ghLatestCommit = ghRepository
            .queryCommits()
            .path(assignmentPath)
            .list()
            .withPageSize(1)
            .iterator()
            .next();

    LocalDateTime committedAt = getCommitDate(ghLatestCommit);

    return new AssignmentSubmission(
            ghContent.getHtmlUrl(), ghLatestCommit.getSHA1(), content.length(), committedAt);
}
```

**참고**: `GHContent.getSize()`는 바이트 단위로 계산하므로, 한글 문자열 길이를 위해 직접 content를 읽어 `String.length()` 사용

### 2.7 에러 처리

| ErrorCode | 발생 조건 |
|-----------|----------|
| `GITHUB_REPOSITORY_NOT_FOUND` | 레포지토리 조회 실패 |
| `GITHUB_USER_NOT_FOUND` | 사용자 조회 실패 |
| `GITHUB_CONTENT_NOT_FOUND` | 파일 내용 조회 실패 |
| `GITHUB_FILE_READ_FAILED` | 파일 읽기 실패 |
| `GITHUB_COMMIT_DATE_FETCH_FAILED` | 커밋 날짜 조회 실패 |

---

## 3. 토스페이먼츠 연동

### 3.1 Feign Client 인터페이스

**파일**: `infra/feign/payment/client/PaymentClient.java`

```java
@FeignClient(
    name = "paymentClient",
    url = "https://api.tosspayments.com",
    configuration = PaymentClientConfig.class)
public interface PaymentClient {

    @PostMapping("/v1/payments/confirm")
    PaymentResponse confirm(@Valid @RequestBody PaymentConfirmRequest request);

    @GetMapping("/v1/payments/{paymentKey}")
    PaymentResponse getPayment(@PathVariable String paymentKey);

    @PostMapping("/v1/payments/{paymentKey}/cancel")
    PaymentResponse cancelPayment(
            @PathVariable String paymentKey,
            @Valid @RequestBody PaymentCancelRequest request);
}
```

### 3.2 API 엔드포인트

| 메서드 | HTTP | 엔드포인트 | 설명 |
|--------|------|-----------|------|
| `confirm` | POST | `/v1/payments/confirm` | 결제 승인 |
| `getPayment` | GET | `/v1/payments/{paymentKey}` | 결제 정보 조회 |
| `cancelPayment` | POST | `/v1/payments/{paymentKey}/cancel` | 결제 취소 |

### 3.3 Basic Auth 인증

**파일**: `infra/feign/payment/config/BasicAuthConfig.java`

```java
@RequiredArgsConstructor
public class BasicAuthConfig {

    private final PaymentProperty paymentProperty;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(paymentProperty.getSecretKey(), "");
    }
}
```

- **username**: 토스페이먼츠 시크릿 키
- **password**: 빈 문자열 (토스페이먼츠 API 스펙)

**설정 파일**: `application-payment.yml`
```yaml
toss:
  secret-key: ${PAYMENT_TOSS_SECRET_KEY:}
```

### 3.4 클라이언트 설정 구성

**파일**: `infra/feign/payment/config/PaymentClientConfig.java`

```java
@Import({BasicAuthConfig.class, PaymentErrorDecoder.class})
public class PaymentClientConfig {}
```

- `@Import`를 통해 설정 클래스 주입
- Feign 클라이언트별 독립적인 설정 구성

### 3.5 에러 처리

**파일**: `infra/feign/payment/error/PaymentErrorDecoder.java`

```java
public class PaymentErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            var paymentErrorDto = objectMapper.readValue(
                response.body().asInputStream(), PaymentErrorDto.class);
            return new CustomPaymentException(
                response.status(), paymentErrorDto.code(), paymentErrorDto.message());
        } catch (IOException e) {
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
```

**CustomPaymentException**:
```java
@Getter
@RequiredArgsConstructor
public class CustomPaymentException extends RuntimeException {
    private final int status;      // HTTP 상태 코드
    private final String code;     // 토스페이먼츠 에러 코드
    private final String message;  // 에러 메시지
}
```

**참고**: 결제 전용 예외(`CustomPaymentException`)는 프로젝트의 기본 예외(`CustomException`)와 별개로 토스페이먼츠 API 응답 구조에 맞게 설계되었습니다.

### 3.6 Request/Response DTO

**PaymentConfirmRequest**:
```java
public record PaymentConfirmRequest(
    @NotBlank String paymentKey,
    @NotBlank @Size(min = 21, max = 21) String orderId,  // 21자 고정
    @Positive Long amount
) {}
```

**PaymentCancelRequest**:
```java
public record PaymentCancelRequest(@NotBlank String cancelReason) {}
```

**PaymentResponse**: 토스페이먼츠 결제 응답의 전체 구조를 반영한 복잡한 중첩 record

| 중첩 Record | 설명 |
|-------------|------|
| `CancelDto` | 취소 정보 |
| `CardDto` | 카드 결제 정보 |
| `TransferDto` | 계좌이체 정보 |
| `ReceiptDto` | 영수증 URL |
| `EasyPayDto` | 간편결제 정보 |
| `FailureDto` | 실패 정보 |
| `CashReceiptDto`, `CashReceiptsDto` | 현금영수증 정보 |

---

## 4. 이메일 연동 (Gmail SMTP)

### 4.1 인터페이스 추상화

**파일**: `global/util/email/MailSender.java`

```java
public interface MailSender {
    void send(String recipient, String subject, String content);
}
```

### 4.2 JavaEmailSender 구현

**파일**: `global/util/email/JavaEmailSender.java`

```java
@Component
@RequiredArgsConstructor
public class JavaEmailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(String recipient, String subject, String content) {
        MimeMessage message = writeMimeMessage(recipient, subject, content);
        javaMailSender.send(message);
    }
}
```

- `MimeMessage`를 사용한 HTML 이메일 발송
- `message.setText(content, "utf-8", "html")`: HTML 형식 지원
- 발신자: `GDSC Hongik <gdsc.hongik@gmail.com>`

### 4.3 Gmail SMTP 설정

**파일**: `global/config/JavaMailSenderConfig.java`

**설정 파일**: `application-email.yml`
```yaml
email:
  gmail:
    login-email: ${GOOGLE_LOGIN_EMAIL}
    password: ${GOOGLE_PASSWORD}

  host: "smtp.gmail.com"
  port: 465
  encoding: "UTF-8"
  protocol: "smtps"

  java-mail-property:
    smtp-auth:
      key: "mail.smtp.auth"
      value: true
    socket-factory:
      port:
        key: "mail.smtp.socketFactory.port"
        value: 465
      fallback:
        key: "mail.smtp.socketFactory.fallback"
        value: false
      class-info:
        key: "mail.smtp.socketFactory.class"
        value: "javax.net.ssl.SSLSocketFactory"
```

| 항목 | 값 |
|------|------|
| Host | smtp.gmail.com |
| Port | 465 (SMTPS) |
| Protocol | smtps (SSL) |
| 인증 | Gmail 앱 비밀번호 |

### 4.4 EmailProperty 구조

**파일**: `global/property/EmailProperty.java`

```java
@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "email")
public class EmailProperty {
    private final Gmail gmail;
    private final String host;
    private final int port;
    private final String protocol;
    private final String encoding;
    private final Map<String, Object> javaMailProperty;

    public record Gmail(String loginEmail, String password) {}
}
```

### 4.5 이메일 인증 토큰

**파일**: `global/util/email/EmailVerificationTokenUtil.java`

**JWT 토큰 구조**:
| 클레임 | 설명 |
|--------|------|
| `sub` | 회원 ID (memberId) |
| `email` | 인증 대상 이메일 |
| `iss` | 발급자 (jwtProperty.issuer) |
| `iat` | 발급 시간 |
| `exp` | 만료 시간 |

**주요 메서드**:
```java
public String generateEmailVerificationToken(Long memberId, String email);
public EmailVerificationTokenDto parseEmailVerificationTokenDto(String token) throws ExpiredJwtException;
```

**토큰 만료 처리**:
- `ExpiredJwtException` → `ErrorCode.EXPIRED_EMAIL_VERIFICATION_TOKEN`
- 기타 예외 → `ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN`

### 4.6 인증 링크 생성

**파일**: `global/util/email/VerificationLinkUtil.java`

**환경별 클라이언트 URL**:
| 환경 | URL |
|------|-----|
| prod | `https://onboarding.wawoo.dev` |
| dev | `https://dev-onboarding.wawoo.dev` |
| local | `https://localhost:5173` |

**생성되는 링크 형식**:
```
https://{환경별_URL}/onboarding/verify-email?token={JWT_TOKEN}
```

### 4.7 인증 메일 발송 플로우

**파일**: `domain/email/application/UnivEmailVerificationLinkSendService.java`

**인증 플로우**:
1. 이메일 중복 검증 (`memberRepository.existsByUnivEmail`)
2. 이메일 유효성 검증 (`univEmailValidator.validateSendUnivEmailVerificationLink`)
3. JWT 인증 토큰 생성
4. Redis에 토큰 저장 (만료 시간 포함)
5. HTML 이메일 발송

**토큰 유효 시간**: 30분 (`VERIFICATION_TOKEN_TIME_TO_LIVE`)

**이메일 템플릿**: 인라인 CSS 스타일링된 HTML
```html
<div style='font-family: "Roboto", sans-serif; ...'>
    <h3>GDSC Hongik 재학생 인증 메일</h3>
    <a href='{링크}'>재학생 인증하기</a>
</div>
```

---

## 5. Sentry 연동

### 5.1 기본 설정

**설정 파일**: `application-sentry.yml`
```yaml
sentry:
  dsn: ${SENTRY_DSN:}
  traces-sample-rate: 1.0
  exception-resolver-order: -2147483647
  environment: ${spring.profiles.active:local}
  send-default-pii: true
  logging:
    minimum-event-level: warn
    minimum-breadcrumb-level: debug

docker:
  tag: ${DOCKER_IMAGE_TAG:}
```

| 설정 | 값 | 설명 |
|------|------|------|
| `traces-sample-rate` | 1.0 | 모든 트랜잭션 추적 (100%) |
| `exception-resolver-order` | -2147483647 | 최우선 예외 처리 |
| `send-default-pii` | true | 개인 식별 정보 포함 전송 |
| `minimum-event-level` | warn | warn 이상 로그를 이벤트로 전송 |
| `minimum-breadcrumb-level` | debug | debug 이상 로그를 breadcrumb으로 기록 |

### 5.2 트랜잭션 필터링

**파일**: `infra/sentry/CustomBeforeSendTransactionCallback.java`

```java
@Component
public class CustomBeforeSendTransactionCallback
    implements SentryOptions.BeforeSendTransactionCallback {

    @Override
    public SentryTransaction execute(SentryTransaction transaction, Hint hint) {
        String transactionEndpoint = transaction.getTransaction();

        if (transactionEndpoint == null) {
            return transaction;
        }

        if (KEYWORDS_TO_IGNORE.stream().anyMatch(transactionEndpoint::contains)) {
            return null;  // 트랜잭션 무시
        }

        return transaction;
    }
}
```

**필터링 키워드**: `infra/common/constant/SentryConstant.java`
```java
public static final String ACTUATOR_KEYWORD = "actuator";
public static final List<String> KEYWORDS_TO_IGNORE = List.of(ACTUATOR_KEYWORD);
```

- `/gdsc-actuator/health`, `/gdsc-actuator/prometheus` 등 Actuator 엔드포인트 트랜잭션 제외
- Prometheus 스크래핑으로 인한 노이즈 방지

### 5.3 Feign 연동

`FeignConfig`의 `SentryCapability` Bean을 통해 Feign 호출에 대한 분산 추적 활성화:
- 외부 API 호출(토스페이먼츠 등) 시 Sentry span 자동 생성
- 호출 지연 시간, 에러 추적 가능

### 5.4 의존성

```groovy
implementation 'io.sentry:sentry-logback:7.14.0'
implementation 'io.sentry:sentry-openfeign:7.14.0'
```

---

## 6. 설정 파일 구조

### 6.1 프로필 그룹 구성

**파일**: `application.yml`
```yaml
spring:
  profiles:
    group:
      test: "test"
      local: "local, datasource"
      dev: "dev, datasource"
      prod: "prod, datasource"
    include:
      - redis
      - security
      - actuator
      - discord
      - email
      - payment
      - github
      - sentry
      - modulith
```

### 6.2 외부 서비스별 설정 파일

| 설정 파일 | 외부 서비스 | 주요 환경 변수 |
|-----------|------------|----------------|
| `application-github.yml` | GitHub API | `GITHUB_SECRET_KEY` |
| `application-payment.yml` | 토스페이먼츠 | `PAYMENT_TOSS_SECRET_KEY` |
| `application-email.yml` | Gmail SMTP | `GOOGLE_LOGIN_EMAIL`, `GOOGLE_PASSWORD` |
| `application-sentry.yml` | Sentry | `SENTRY_DSN`, `DOCKER_IMAGE_TAG` |
| `application-discord.yml` | Discord | `DISCORD_BOT_TOKEN`, `DISCORD_SERVER_ID`, ... |

### 6.3 환경 변수 바인딩 패턴

```yaml
# 기본값 없음 (필수)
secret-key: ${GITHUB_SECRET_KEY:}

# 기본값 있음 (선택)
host: ${REDIS_HOST:localhost}
port: ${REDIS_PORT:6379}
```

---

## 7. 새로운 외부 서비스 추가 가이드

### 7.1 Feign Client 방식

1. **Client 인터페이스 정의**
   ```java
   @FeignClient(
       name = "externalClient",
       url = "https://api.external.com",
       configuration = ExternalClientConfig.class)
   public interface ExternalClient {
       // API 메서드 정의
   }
   ```

2. **설정 클래스 작성** (`infra/feign/external/config/`)
   - 인증 설정 (Basic Auth, Bearer Token 등)
   - 에러 디코더 설정

3. **에러 처리** (`infra/feign/external/error/`)
   - 커스텀 ErrorDecoder 구현
   - 서비스별 예외 클래스 정의

4. **설정 파일 추가** (`application-external.yml`)
   - API 키, 시크릿 등 환경 변수 바인딩

5. **프로필 include** (`application.yml`)
   ```yaml
   spring.profiles.include:
     - external
   ```

### 7.2 전용 라이브러리 방식 (GitHub 예시)

1. **의존성 추가** (`build.gradle`)
   ```groovy
   implementation 'org.example:external-api:1.0.0'
   ```

2. **Bean 설정** (`global/config/ExternalConfig.java`)
   ```java
   @Bean
   public ExternalClient externalClient() {
       return ExternalClientBuilder.build(property.getApiKey());
   }
   ```

3. **Client 래퍼 클래스** (`infra/external/client/`)
   - 라이브러리 호출을 감싸는 서비스 클래스
   - 에러 변환 및 예외 처리

---

## 참고

- Discord 연동: [DISCORD.md](DISCORD.md) 참조
- 인프라 설정: [INFRASTRUCTURE.md](INFRASTRUCTURE.md) 참조
- 예외 처리: [EXCEPTION.md](EXCEPTION.md) 참조
