# LOCK.md

이 문서는 프로젝트의 분산 락 구현 및 사용 방법에 대해 설명합니다.

---

## 개요

MySQL Named Lock을 사용한 동시성 제어 메커니즘입니다. 동일한 리소스에 대한 동시 접근을 제어하여 데이터 정합성을 보장합니다.

### 사용 목적

이벤트 도메인에서 다음과 같은 동시성 문제를 해결하기 위해 사용됩니다:

- **이벤트 수정 중 신청 방지**: 이벤트 정책(참가 인원 제한 등)을 수정하는 동안 이벤트 신청이 발생하면 데이터 정합성 문제가 발생할 수 있습니다. 분산 락을 통해 이벤트 수정이 완료될 때까지 해당 이벤트에 대한 신청을 차단합니다.

---

## 구성 요소

### 파일 구조

```
src/main/java/com/gdschongik/gdsc/global/lock/
├── DistributedLock.java    # 분산 락 어노테이션
├── LockAspect.java         # AOP Aspect
├── LockUtil.java           # 락 유틸리티 인터페이스
└── MySqlLockUtil.java      # MySQL Named Lock 구현체
```

### 1. `@DistributedLock` 어노테이션

메서드에 분산 락을 적용하기 위한 어노테이션입니다.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key();                    // 락 식별자 (SpEL 사용 가능), 필수값
    int timeoutSec() default 5;      // 락 획득 대기 시간 (초)
}
```

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `key` | 락 식별자. SpEL 표현식 지원. **필수값** | 없음 |
| `timeoutSec` | 락 획득 최대 대기 시간 (초). 이 시간 내에 락을 획득하지 못하면 예외 발생 | 5 |

### 2. `LockAspect`

AOP를 통해 `@DistributedLock` 어노테이션을 처리합니다.

**주요 기능**:
1. SpEL을 사용한 동적 락 키 파싱
2. `LockUtil`을 통한 락 획득/해제
3. try-finally 블록으로 락 해제 보장

**락 키 생성 규칙**:
- key가 빈 문자열인 경우: `클래스명:메서드명` 형식의 기본 키 생성
- key가 있는 경우: SpEL 표현식으로 평가

### 3. `LockUtil` 인터페이스

락 구현체를 추상화한 인터페이스입니다. 현재는 `MySqlLockUtil`만 구현되어 있으나, 향후 Redis 등 다른 락 구현체 도입을 고려할 수 있습니다.

```java
public interface LockUtil {
    boolean acquireLock(String key, long timeoutSec);
    boolean releaseLock(String key);
}
```

### 4. `MySqlLockUtil`

MySQL Named Lock을 사용한 `LockUtil` 구현체입니다.

**사용 쿼리**:
- 락 획득: `SELECT GET_LOCK(?, ?)`
- 락 해제: `SELECT RELEASE_LOCK(?)`

**결과 해석**:
| 함수 | 결과값 | 의미 |
|------|--------|------|
| `GET_LOCK` | 1 | 락 획득 성공 |
| `GET_LOCK` | 0 | 타임아웃 (대기 시간 초과) |
| `GET_LOCK` | null | 에러 발생 |
| `RELEASE_LOCK` | 1 | 락 해제 성공 |
| `RELEASE_LOCK` | 0 | 락이 해당 세션의 것이 아님 |
| `RELEASE_LOCK` | null | 락이 존재하지 않음 |

---

## 사용 방법

### 기본 사용법

```java
@DistributedLock(key = "'resource:' + #resourceId")
@Transactional
public void updateResource(Long resourceId, UpdateRequest request) {
    // 비즈니스 로직
}
```

### SpEL 표현식 예시

```java
// 단순 파라미터 참조
@DistributedLock(key = "'event:' + #eventId")

// DTO 필드 참조
@DistributedLock(key = "'event:' + #request.eventId()")

// 여러 파라미터 조합
@DistributedLock(key = "'user:' + #userId + ':event:' + #eventId")
```

### 실제 사용 사례

#### 이벤트 기본 정보 수정

```java
// EventService.java:78
@DistributedLock(key = "'event:' + #eventId")
@Transactional
public void updateEventBasicInfo(Long eventId, EventUpdateBasicInfoRequest request) {
    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
    // 이벤트 정보 수정
}
```

#### 온라인 이벤트 신청

```java
// EventParticipationService.java:308
@DistributedLock(key = "'event:' + #request.eventId()")
@Transactional
public void applyOnline(EventApplyOnlineRequest request) {
    // 이벤트 신청 처리
}
```

**락 키 패턴**: 동일한 이벤트에 대해 `event:{eventId}` 형식의 동일한 락 키를 사용하여, 이벤트 수정과 이벤트 신청이 동시에 발생하지 않도록 제어합니다.

---

## 동작 흐름

### 현재 구현 (개선 필요)

```
1. LockAspect 진입 (@Order(1)로 트랜잭션보다 먼저 실행)
2. 락 키 파싱 (SpEL 평가)
3. MySQL Named Lock 획득 시도
   └─ 실패 시: LOCK_ACQUIRE_FAILED 예외 발생
4. 트랜잭션 시작 (@Transactional AOP)
5. 비즈니스 로직 실행
6. 트랜잭션 종료 (커밋 또는 롤백)
7. LockAspect finally 블록에서 락 해제
```

---

## 예외 처리

### ErrorCode

```java
LOCK_ACQUIRE_FAILED(INTERNAL_SERVER_ERROR, "락 획득에 실패했습니다. 다시 시도해주세요.")
```

락 획득에 실패하면 (타임아웃 또는 에러) `CustomException`이 발생합니다.

---

## 주의사항

### 1. 반드시 `@Transactional`과 함께 사용

분산 락은 트랜잭션과 함께 사용되어야 합니다. 트랜잭션 없이 사용하면 데이터 정합성을 보장할 수 없습니다.

```java
// 올바른 사용
@DistributedLock(key = "'event:' + #eventId")
@Transactional
public void updateEvent(Long eventId) { ... }

// 잘못된 사용 - 트랜잭션 없음
@DistributedLock(key = "'event:' + #eventId")
public void updateEvent(Long eventId) { ... }
```

### 2. 락 키는 반드시 명시적으로 지정

key는 필수값입니다. 의미 있는 락 키를 명시적으로 지정하세요.

```java
// 권장: 명확한 락 키
@DistributedLock(key = "'event:' + #eventId")

// 비권장: 빈 문자열 (기본값 사용)
@DistributedLock(key = "")  // 클래스명:메서드명으로 생성됨
```

### 3. 타임아웃 설정 고려

기본 타임아웃은 5초입니다. 비즈니스 로직의 예상 실행 시간을 고려하여 적절한 타임아웃을 설정하세요.

```java
// 긴 작업의 경우 타임아웃 증가
@DistributedLock(key = "'batch:' + #batchId", timeoutSec = 30)
```

### 4. 락 키 설계 시 범위 고려

락 키의 범위가 너무 넓으면 불필요한 대기가 발생하고, 너무 좁으면 동시성 제어가 제대로 되지 않습니다.

```java
// 적절한 범위: 특정 이벤트에 대한 락
@DistributedLock(key = "'event:' + #eventId")

// 너무 넓은 범위: 모든 이벤트에 대한 락 (병목 발생)
@DistributedLock(key = "'event'")
```

---

## 관련 문서

- [DOMAIN.md](./DOMAIN.md) - 도메인 아키텍처 및 코딩 컨벤션
- [EXCEPTION.md](./EXCEPTION.md) - 예외 처리 전략
