# EVENT.md

이 문서는 gdsc-server 프로젝트의 도메인 이벤트 전략을 설명합니다.

---

## 개요

도메인 간 느슨한 결합과 비동기 처리를 위해 도메인 이벤트를 사용합니다:

- 도메인 엔티티에서 `registerEvent()`로 이벤트 발행
- `@ApplicationModuleListener`로 트랜잭션 커밋 후 비동기 이벤트 처리
- `EventRetryManager`를 통한 트랜잭션 아웃박스 패턴으로 정합성 보장
- 외부 메시징 시스템(RabbitMQ 등)은 사용하지 않음

### 구현 기술

Spring Modulith의 이벤트 레지스트리 기능을 활용합니다:

```gradle
dependencyManagement {
    imports {
        mavenBom 'org.springframework.modulith:spring-modulith-bom:1.3.3'
    }
}

dependencies {
    implementation 'org.springframework.modulith:spring-modulith-starter-jpa'
}
```

`spring-modulith-starter-jpa`는 JPA 엔티티를 활용한 이벤트 퍼시스턴스를 제공합니다. 이벤트 발행 시 `event_publication` 테이블에 저장되어 재시도가 가능합니다.

---

## 도메인 이벤트 발행

### BaseEntity와 AbstractAggregateRoot

모든 엔티티는 `BaseEntity`를 상속하며, `BaseEntity`는 `AbstractAggregateRoot`를 상속합니다:

```java
@MappedSuperclass
public abstract class BaseEntity extends AbstractAggregateRoot<BaseEntity> {
    // Auditing 필드
}
```

### 이벤트 발행 방식

#### 1. 도메인 메서드 내부 발행 (권장)

단일 엔티티의 상태 변경에 따른 이벤트는 도메인 메서드 내부에서 `registerEvent()`로 발행합니다:

```java
// Member.java
public void advanceToRegular() {
    validateAdvanceToRegular();
    role = REGULAR;
    registerEvent(new MemberAdvancedToRegularEvent(id, discordId));
}

public void demoteToAssociate() {
    validateDemoteToAssociate();
    role = ASSOCIATE;
    registerEvent(new MemberDemotedToAssociateEvent(id, discordId));
}
```

#### 2. JPA 라이프사이클 콜백 발행

엔티티 생성/삭제 시점에 이벤트를 발행해야 하는 경우 JPA 콜백을 사용합니다:

```java
// StudyHistoryV2.java
@PostPersist
private void postPersist() {
    registerEvent(new StudyApplyCompletedEvent(
        this.study.getDiscordRoleId(),
        this.student.getDiscordId()));
}

@PreRemove
private void preRemove() {
    registerEvent(new StudyApplyCanceledEvent(
        this.study.getDiscordRoleId(),
        this.student.getDiscordId(),
        this.study.getId(),
        this.student.getId()));
}
```

#### 3. ApplicationEventPublisher 직접 사용

여러 엔티티의 집계 이벤트나 서비스 레이어에서만 발행 가능한 이벤트는 `ApplicationEventPublisher`를 직접 사용합니다:

```java
// MentorStudyHistoryServiceV2.java
@Service
@RequiredArgsConstructor
public class MentorStudyHistoryServiceV2 {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void completeStudy(StudyCompleteRequest request) {
        List<StudyHistoryV2> studyHistories = // 여러 스터디 히스토리 조회
        studyHistories.forEach(StudyHistoryV2::complete);

        // 여러 ID를 모아서 단일 이벤트로 발행
        applicationEventPublisher.publishEvent(
            new StudyHistoriesCompletedEvent(
                studyHistories.stream()
                    .map(StudyHistoryV2::getId)
                    .toList()));
    }
}
```

### 이벤트 발행 방식 선택 기준

| 방식 | 용도 | 사용 위치 |
|------|------|----------|
| `registerEvent()` | 단일 엔티티 상태 변경 이벤트 | 엔티티 도메인 메서드 |
| JPA 콜백 + `registerEvent()` | 엔티티 생성/삭제 이벤트 | 엔티티 @PostPersist, @PreRemove |
| `ApplicationEventPublisher.publishEvent()` | 다건 처리, 집계 이벤트 | 서비스 계층 |

---

## 도메인 이벤트 클래스

### 위치 및 네이밍

- **위치**: `domain/[도메인]/domain/event/` 패키지
- **네이밍**: `[Entity][PastTenseVerb]Event` 형식
  - 도메인 이벤트는 "이미 발생한 사실"을 나타내므로 과거형 동사를 사용합니다
  - 예시: `OrderCreatedEvent`, `MemberAdvancedToRegularEvent`, `PaymentCompletedEvent`

### 구조

모든 이벤트는 `record`로 정의합니다:

```java
// 단순 식별자만 포함
public record MemberAssociateRequirementUpdatedEvent(Long memberId) {}

// 여러 필드 포함
public record MemberAdvancedToRegularEvent(Long memberId, String discordId) {}

// 컬렉션 포함 (NonNull 검증)
public record StudyHistoriesCompletedEvent(@NonNull List<Long> studyHistoryIds) {}
```

### 이벤트 목록

| 도메인 | 이벤트 | 발행 시점 |
|--------|--------|----------|
| member | MemberAdvancedToRegularEvent | 정회원 승급 시 |
| member | MemberAssociateRequirementUpdatedEvent | 준회원 요건 충족 시 |
| member | MemberDemotedToAssociateEvent | 준회원 강등 시 |
| membership | MembershipVerifiedEvent | 멤버십 결제 검증 완료 시 |
| membership | MembershipPaymentRevokedEvent | 멤버십 결제 철회 시 |
| order | OrderCreatedEvent | 주문 생성 시 |
| order | OrderCompletedEvent | 주문 완료 시 |
| order | OrderCanceledEvent | 주문 취소 시 |
| studyv2 | StudyApplyCompletedEvent | 스터디 수강신청 완료 시 (@PostPersist) |
| studyv2 | StudyApplyCanceledEvent | 스터디 수강신청 취소 시 (@PreRemove) |
| studyv2 | StudyAnnouncementCreatedEvent | 스터디 공지 생성 시 (@PostPersist) |
| study | StudyHistoriesCompletedEvent | 스터디 일괄 수료 처리 시 |
| study | StudyHistoryCompletionWithdrawnEvent | 스터디 수료 철회 시 |

---

## 이벤트 처리

### @ApplicationModuleListener

모든 이벤트 핸들러는 `@ApplicationModuleListener`를 사용합니다:

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final MembershipService membershipService;

    @ApplicationModuleListener
    public void handleOrderCompletedEvent(OrderCompletedEvent event) {
        log.info("[OrderEventHandler] 주문 완료 이벤트 수신: nanoId={}", event.nanoId());
        membershipService.verifyPaymentStatus(event.nanoId());
    }
}
```

### @ApplicationModuleListener 특성

- `@Async` + `@TransactionalEventListener(AFTER_COMMIT)`의 조합
- 원본 트랜잭션 커밋 후 별도 스레드에서 비동기 실행
- 실패 시 원본 트랜잭션에 영향 없음
- `EventPublication`에 저장되어 재시도 대상

### 핸들러 네이밍 컨벤션

용도에 따라 네이밍을 구분합니다:

| 네이밍 | 용도 | 예시 |
|--------|------|------|
| `*EventHandler` | 도메인 내부 이벤트 처리 | OrderEventHandler, MemberEventHandler |
| `*EventListener` | 외부 시스템 연동 (Discord 등) | DelegateMemberDiscordEventListener |

### 메서드 네이밍 컨벤션

- **패턴**: `handle[EventName]` 또는 동사형
- **예시**: `handleOrderCompletedEvent`, `delegateMemberDiscordEvent`

---

## 이벤트 재시도 전략

### EventRetryManager

**파일**: `src/main/java/com/gdschongik/gdsc/global/modulith/EventRetryManager.java`

Spring Modulith의 `IncompleteEventPublications`를 활용하여 미처리 이벤트를 재시도합니다.

**동작 방식**:
- `retryIncompleteEvents()`: 5초 간격으로 실행. 10~30초 경과된 미처리 이벤트를 재시도
- `logDeadLetterEvents()`: 60분 간격으로 실행. 30초 초과 경과된 데드레터 이벤트를 로그 경고

### 재시도 설정

| 설정 | 값 | 설명 |
|------|-----|------|
| RETRY_INTERVAL_SECOND | 5 | 재시도 스케줄러 실행 간격 (초) |
| MIN_RETRY_AGE_SECOND | 10 | 재시도 시작 경과 시간 (초) |
| MAX_RETRY_AGE_SECOND | 30 | 데드레터 판정 경과 시간 (초) |
| DLQ_INTERVAL_MINUTE | 60 | 데드레터 확인 간격 (분) |

### 재시도 흐름

```
이벤트 발행
    ↓
event_publication 테이블 저장
    ↓
이벤트 핸들러 실행
    ↓
성공 → 레코드 삭제  |  실패 → 레코드 유지
                        ↓
                   10초 경과 후 재시도 대상
                        ↓
                   5초 간격으로 재시도
                        ↓
                   30초 경과 시 데드레터 판정
                        ↓
                   로그 경고 (재시도 중단)
```

### 트랜잭션 아웃박스 패턴

`@ApplicationModuleListener`와 `EventRetryManager`의 조합은 트랜잭션 아웃박스 패턴을 구현합니다:

1. 원본 트랜잭션에서 이벤트가 `event_publication` 테이블에 저장됨
2. 트랜잭션 커밋 후 비동기로 이벤트 핸들러 실행
3. 핸들러 실패 시에도 원본 트랜잭션은 이미 커밋된 상태
4. `EventRetryManager`가 미처리 이벤트를 재시도하여 최종 정합성 보장

---

## 참고 문서

상위 문서:

- [CLAUDE.md](CLAUDE.md) - 프로젝트 전체 개요로 돌아가기

관련 문서:

- [DOMAIN.md](DOMAIN.md) - 도메인 이벤트 발행/처리 구현 위치 (엔티티, 이벤트 핸들러)
