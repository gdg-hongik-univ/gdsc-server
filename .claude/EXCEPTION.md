# EXCEPTION.md

이 문서는 gdsc-server 프로젝트의 공용 예외 처리 방법에 대해 설명합니다.

---

## 예외 처리 전략

- 모든 비즈니스 로직 예외는 `CustomException`을 사용하여 처리합니다.
- 비즈니스 로직에서 발생할 수 있는 모든 예외 상황별 상태 코드와 메시지를 `ErrorCode` Enum으로 관리합니다.
- 예외 발생 시 상황에 맞는 `ErrorCode`를 담아 `new CustomException(ErrorCode)`으로 예외를 생성합니다.

---

## CustomException

**파일**: `global/exception/CustomException.java`

### 클래스 구조

```java
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
```

### 사용 패턴

- `RuntimeException`을 상속하여 언체크 예외로 처리
- 생성자를 통한 직접 인스턴스 생성

```java
// 기본 메시지 사용
throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);

// 커스텀 메시지 사용
throw new CustomException(ErrorCode.INVALID_PARAMETER, "특정 파라미터가 잘못되었습니다.");
```

---

## ErrorCode

**파일**: `global/exception/ErrorCode.java`

### Enum 구조

```java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "내부 서버 에러가 발생했습니다. 관리자에게 문의 바랍니다."),
    // ...
    ;
    private final HttpStatus status;
    private final String message;
}
```

### 필드 구성

| 필드 | 타입 | 설명 |
|------|------|------|
| status | HttpStatus | HTTP 상태 코드 |
| message | String | 에러 메시지 |

### 도메인별 그룹핑

ErrorCode는 주석으로 도메인별로 그룹핑됩니다:

- 공통 (INTERNAL_ERROR, METHOD_ARGUMENT_INVALID 등)
- Auth
- Parameter
- Money / Period
- Member / Requirement
- Univ Email Verification
- Discord
- Membership
- Recruitment / RecruitmentRound
- Coupon
- Study / StudyDetail / StudyHistory / StudyAnnouncement / StudyAchievement
- Attendance
- Order / MoneyInfo
- Assignment / AssignmentHistory
- Github
- Excel
- Event
- Lock

### 네이밍 규칙

- 패턴: `DOMAIN_ACTION_DESCRIPTION` 또는 `DOMAIN_DESCRIPTION`
- 예시:
  - `MEMBER_NOT_FOUND` - 도메인_상태
  - `STUDY_NOT_APPLICABLE` - 도메인_동작불가사유
  - `ORDER_DISCOUNT_AMOUNT_MISMATCH` - 도메인_필드_불일치

---

## GlobalExceptionHandler

**파일**: `global/exception/GlobalExceptionHandler.java`

### 개요

- `@RestControllerAdvice`를 사용하여 전역 예외 처리
- `ResponseEntityExceptionHandler`를 확장하여 Spring MVC 표준 예외 처리 지원
- HTTP 요청 처리 중 발생한 모든 예외를 중앙에서 처리

### 처리하는 예외

| 예외 타입 | 로그 레벨 | 응답 |
|-----------|-----------|------|
| CustomException | INFO | ErrorCode의 status, message |
| CustomPaymentException | INFO | 결제 API 에러 코드, 메시지 |
| MethodArgumentNotValidException | INFO | 400 METHOD_ARGUMENT_INVALID |
| Exception | ERROR | 500 INTERNAL_ERROR |

### 예외 처리 흐름

```
예외 발생
    ↓
GlobalExceptionHandler
    ↓
예외 타입별 핸들러 메서드 실행
    ↓
로그 기록 (INFO/ERROR)
    ↓
ErrorResponse 생성 및 반환
```

### ErrorResponse 구조

**파일**: `global/exception/ErrorResponse.java`

```java
public record ErrorResponse(String errorCodeName, String errorMessage) {
    public static ErrorResponse of(ErrorCode errorCode) { ... }
    public static ErrorResponse of(ErrorCode errorCode, String errorMessage) { ... }
    public static ErrorResponse of(String errorCodeName, String errorMessage) { ... }
}
```

### 예외별 처리 상세

#### CustomException

```java
@ExceptionHandler(CustomException.class)
public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    log.info("CustomException : {}", e.getMessage());
    return ResponseEntity.status(e.getErrorCode().getStatus())
        .body(ErrorResponse.of(e.getErrorCode()));
}
```

#### MethodArgumentNotValidException

- `@Valid` 검증 실패 시 호출
- 첫 번째 검증 오류 메시지를 추출하여 응답

```java
@Override
protected ResponseEntity<Object> handleMethodArgumentNotValid(...) {
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    return ResponseEntity.status(status.value())
        .body(ErrorResponse.of(ErrorCode.METHOD_ARGUMENT_INVALID, errorMessage));
}
```

#### 미처리 예외 (Exception)

- 예상하지 못한 모든 예외를 캐치
- ERROR 레벨로 로깅 후 500 응답

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("INTERNAL_SERVER_ERROR : {}", e.getMessage());
    return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
        .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
}
```

---

## Sentry 연동

예외 모니터링을 위해 Sentry를 사용합니다.

### 관련 파일

| 파일 | 경로 | 역할 |
|------|------|------|
| SentryConfig.java | `global/config/` | Sentry 설정 |
| SentryConstant.java | `global/common/constant/` | Sentry 상수 |
| CustomBeforeSendTransactionCallback.java | `infra/sentry/` | 트랜잭션 필터링 |

### SentryConfig

**릴리즈 버전 설정**:
- Docker 태그에서 릴리즈 버전 추출
- 형식: `gdsc-server@버전` (예: `gdsc-server@1.0.0`)

**무시할 예외 설정**:
```java
private final List<Class<? extends Throwable>> exceptionsToIgnore = List.of(
    NoResourceFoundException.class,      // 존재하지 않는 정적 리소스 요청
    MethodArgumentNotValidException.class // @Valid 검증 실패
);
```

### 트랜잭션 필터링

`CustomBeforeSendTransactionCallback`에서 불필요한 트랜잭션을 필터링합니다:

- `actuator` 키워드가 포함된 트랜잭션은 Sentry에 전송하지 않음

---

## 관련 문서

### Discord 예외 처리

Discord 봇 관련 예외 처리는 별도의 AOP 기반 아키텍처로 구현되어 있습니다.
자세한 내용은 [DISCORD.md](./DISCORD.md)를 참조하세요.

### Feign 클라이언트 예외 처리

결제 API 등 외부 서비스 연동 시 발생하는 예외(CustomPaymentException)는 Feign 클라이언트에서 처리됩니다.
자세한 내용은 [EXTERNAL.md](./EXTERNAL.md)를 참조하세요.

---

## 향후 검토 필요 사항

### 스케줄러 예외 처리

현재 `@Scheduled` 메서드(예: EventRetryManager)에서 발생하는 예외에 대한 별도 핸들러가 없습니다.
Spring 기본 처리(로깅)로 동작하며, Sentry 전송 등 추가 처리가 필요한지 검토가 필요합니다.
