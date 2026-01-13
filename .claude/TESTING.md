# TESTING.md

이 문서는 프로젝트의 테스트 구조, 작성 가이드, 테스트 명령어를 설명합니다.

---

## 테스트 라이브러리

- 테스트 프레임워크: JUnit 5 (Jupiter)
- 어설션 라이브러리: AssertJ
- 모킹 프레임워크: Mockito
- 스프링 통합 테스트: Spring Boot Test, Spring Security Test

---

## 테스트 구조

### 테스트 계층

디렉토리 구조: `src/test/java/com/gdschongik/gdsc/`

```
src/test/java/com/gdschongik/gdsc/
├── config/                     # 테스트 전용 설정 클래스
│   ├── TestQuerydslConfig.java
│   ├── TestSyncExecutorConfig.java
│   ├── TestRedisConfig.java
│   └── TestLockConfig.java
├── helper/                     # 테스트 헬퍼 클래스
│   ├── IntegrationTest.java    # 통합 테스트 베이스
│   ├── DatabaseCleaner.java
│   ├── RedisCleaner.java
│   ├── FixtureHelper.java
│   └── InmemoryLockUtil.java
├── domain/                     # 도메인별 테스트
│   └── [domain]/
│       ├── application/        # 애플리케이션 서비스 통합 테스트
│       └── domain/             # 도메인 로직 단위 테스트
└── global/
    └── common/
        └── constant/           # 테스트 상수
```

### 테스트 환경 설정

프로필: `test`

데이터베이스:
- 프로덕션: MySQL 8.0+
- 테스트: H2 인메모리 DB (MySQL 모드)

비동기 처리:
- 프로덕션: 스레드 풀 기반 비동기
- 통합 테스트: `TestSyncExecutorConfig`로 동기 처리
- 테스트의 결정성 보장

---

## 테스트 작성 가이드

### 테스트 이름 지정 관례

테스트 클래스 이름:
- `{클래스}Test`
- 예: `MemberServiceTest`, `OrderValidatorTest`

테스트 메서드 이름:
- 상황과 결과를 설명하는 이름을 한글로 작성
- 같은 상황에서 여러 케이스의 테스트가 필요한 경우 `@Nested`와 inner class를 활용해 작성
- inner 클래스의 이름은 `...할때` 또는 `...시`로 작성
- 메서드 이름은 `...한다`로 상황과 결과가 이어지게 작성

```java
class MemberTest {

    @Nested
    class 게스트_회원가입시 {

        @Test
        void MemberRole은_GUEST이다() { }

        @Test
        void MemberStatus는_NORMAL이다() { }
    }

    @Nested
    class 준회원으로_승급시도시 {

        @Test
        void 기본_회원정보_작성하지_않았으면_실패한다() { }

        @Test
        void 모든_준회원_가입조건이_인증되었으면_성공한다() { }
    }
}
```

### 테스트 종류

프로젝트의 테스트는 두 가지 종류로 나뉩니다:
- **단위 테스트**: 엔티티 도메인 로직, 도메인 서비스 로직 검증 (Spring 의존성 X)
- **통합 테스트**: 애플리케이션 서비스 로직 검증 (`IntegrationTest` 상속으로 애플리케이션 컨텍스트 재활용)

### 검증 관례

- AssertJ를 사용하여 검증합니다.
- 검증 메서드는 항상 와일드카드 static import를 사용합니다.

```java
import static org.assertj.core.api.Assertions.*;
```

- 비교 및 검증에 사용되는 주요 상수는 given 단계에서 미리 변수로 지정해둡니다.

---

## 단위 테스트 (Unit Tests)

Spring 의존성 없이 엔티티와 도메인 서비스의 비즈니스 로직을 검증합니다.

테스트 위치: `src/test/java/com/gdschongik/gdsc/domain/[domain]/domain/`

### 기본 원칙

- Spring 컨텍스트를 로드하지 않음
- 순수 Java 객체로 테스트 작성
- 빠른 실행 속도로 즉각적인 피드백 가능
- 비즈니스 규칙 검증에 집중

### 테스트 작성 방법

- JUnit 5 `@Test` 어노테이션 사용
- given-when-then 패턴으로 테스트 구조화
- 메서드 호출 후 상태 변화 검증

```java
class MemberTest {

    @Nested
    class 게스트_회원가입시 {

        @Test
        void MemberRole은_GUEST이다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            MemberRole role = member.getRole();

            // then
            assertThat(role).isEqualTo(MemberRole.GUEST);
        }
    }
}
```

### 검증 방식 (AssertJ)

```java
// 기본 검증
assertThat(role).isEqualTo(MemberRole.GUEST);
assertThat(members).hasSize(3);
assertThat(members).contains(member);

// 복합 검증
assertThat(member)
        .extracting(
                Member::getRole,
                Member::getUnivEmail,
                Member::getName)
        .containsExactly(GUEST, null, null);
```

### 예외 처리 테스트

```java
// 예외 발생 검증
assertThatThrownBy(member::advanceToAssociate)
        .isInstanceOf(CustomException.class)
        .hasMessage(INFO_NOT_SATISFIED.getMessage());

// 예외 미발생 검증
assertThatCode(() -> orderValidator.validateCompleteOrder(...))
        .doesNotThrowAnyException();
```

### 데이터 준비

- 테스트 내에서 필요한 객체를 직접 생성 혹은 `FixtureHelper` 사용
- 최소한의 필수 데이터만 포함
- 명확한 변수명으로 의도 드러내기

```java
class OrderValidatorTest {

    FixtureHelper fixtureHelper = new FixtureHelper();
    OrderValidator orderValidator = new OrderValidator();

    public Member createAssociateMember(Long id) {
        return fixtureHelper.createAssociateMember(id);
    }

    @Nested
    class 임시주문_생성_검증할때 {

        @Test
        void 멤버십_대상_멤버와_현재_로그인한_멤버_다르면_실패한다() {
            // given
            Member currentMember = createAssociateMember(1L);
            // ...
        }
    }
}
```

---

## 통합 테스트 (Integration Tests)

Spring 컨텍스트를 로드하여 애플리케이션 서비스의 통합 로직을 검증합니다.

테스트 위치: `src/test/java/com/gdschongik/gdsc/domain/[domain]/application/`

### 기본 설정

- `IntegrationTest`를 상속받아 공통 설정 사용, 애플리케이션 컨텍스트 재활용
- `@Autowired`로 필요한 서비스, 저장소 주입
- `@Test`로 테스트 메서드 정의

```java
class OrderServiceTest extends IntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Nested
    class 임시주문_생성할때 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            logoutAndReloginAs(1L, MemberRole.ASSOCIATE);
            RecruitmentRound recruitmentRound = createRecruitmentRound();
            Membership membership = createMembership(member, recruitmentRound);

            // when
            var request = new OrderCreateRequest(...);
            orderService.createPendingOrder(request);

            // then
            assertThat(orderRepository.findAll()).hasSize(1);
        }
    }
}
```

### 테스트 작성 방법

- given-when-then 패턴으로 테스트 작성
- 단순 조회 로직은 테스트 필수 X (복잡한 조회 결과 혹은 데이터 상태 변경 로직 위주 테스트)
- 도메인 로직 단위 테스트에서 검증하는 내용은 중복 검증 X
- 애플리케이션 서비스 간 흐름과 외부 시스템 통합 검증

### 데이터 준비 (given 단계)

**방법 1: IntegrationTest 헬퍼 메서드 이용 (권장)**

여러 도메인에 걸쳐 공통적으로 사용되는 기본 데이터 세팅에 활용:

```java
// IntegrationTest.java에 정의된 헬퍼 메서드
protected Member createMember() { ... }
protected Member createGuestMember() { ... }
protected Member createAssociateMember() { ... }
protected Member createRegularMember() { ... }
protected RecruitmentRound createRecruitmentRound() { ... }
protected Membership createMembership(Member member, RecruitmentRound recruitmentRound) { ... }
```

**방법 2: 서비스 이용 저장**

요청 DTO 생성 후 애플리케이션 서비스의 생성 메서드로 저장:

```java
@Test
void 성공한다() {
    // given
    var createRequest = new OrderCreateRequest(...);
    orderService.createPendingOrder(createRequest);

    // when
    var completeRequest = new OrderCompleteRequest(...);
    orderService.completeOrder(completeRequest);

    // then
    ...
}
```

**방법 3: 직접 저장 (비권장)**

간단한 테스트에만 사용, 비즈니스 로직을 우회하므로 제한적 사용:

```java
Member member = Member.createGuest(OAUTH_ID);
memberRepository.save(member);
```

### 외부 서비스 모킹

`IntegrationTest`에서 외부 서비스는 `@MockBean`으로 자동 모킹됩니다:

```java
// IntegrationTest.java
@MockBean
protected PaymentClient paymentClient;

@MockBean
protected GithubClient githubClient;

@MockBean
protected DelegateMemberDiscordEventHandler delegateMemberDiscordEventHandler;
```

하위 클래스에서 스텁 커스터마이징이 필요한 경우 `doStubTemplate()` 오버라이드:

```java
class OrderServiceTest extends IntegrationTest {

    @Override
    protected void doStubTemplate() {
        stubPaymentConfirm();
    }

    private void stubPaymentConfirm() {
        PaymentResponse mockPaymentResponse = mock(PaymentResponse.class);
        when(mockPaymentResponse.approvedAt()).thenReturn(ZonedDateTime.now());
        when(paymentClient.confirm(any(PaymentConfirmRequest.class))).thenReturn(mockPaymentResponse);
    }
}
```

### 데이터베이스 자동 정리

- `IntegrationTest`의 `@BeforeEach` 메서드에서 DB/Redis 정리 로직이 자동 활성화
- 각 테스트 시작 전 `DatabaseCleaner`, `RedisCleaner` 실행
- AUTO_INCREMENT 값을 1로 재설정
- 테스트 간 데이터 격리 보장

### 이벤트 핸들러 테스트 주의사항

통합 테스트에서는 이벤트 발행/수신 결과를 검증하면 안 됩니다:

- 현행 구조에서는 이벤트 수신 대기 로직이 없음
- `@ApplicationModuleListener`로 이벤트가 비동기 처리됨
- 비동기 타이밍 이슈로 간헐적 테스트 실패 발생 가능

이벤트 기반 흐름 테스트는 향후 Scenario API 도입으로 해결 예정입니다.

---

## 시나리오 인수 테스트 (Scenario Acceptance Test)

> **참고**: 현재 프로젝트에는 Scenario API 기반 인수 테스트가 구현되어 있지 않습니다.
> 향후 도입 시 아래 가이드를 참고하세요.

Spring Modulith의 Scenario API를 기반으로 비동기 이벤트 처리를 포함한 전체 시스템 동작을 E2E로 테스트합니다.

### 언제 사용해야 하는가?

인수 테스트가 필요한 경우:
- 도메인 이벤트 발행 → 다른 도메인의 이벤트 핸들러 → 최종 상태 변화를 검증해야 할 때
- 여러 애플리케이션 서비스가 이벤트 체인으로 연결된 복잡한 흐름을 테스트할 때
- 비동기 이벤트 처리의 최종 결과를 E2E로 검증해야 할 때

통합 테스트로 충분한 경우:
- 단일 애플리케이션 서비스의 동작만 검증하면 될 때
- 이벤트가 발행되지 않거나 테스트하려는 도메인에 영향을 주지 않을 때
- 동기적인 처리 흐름만 존재할 때

### 구현 가이드

테스트 위치: `src/test/java/com/gdschongik/gdsc/acceptance/`
헬퍼 클래스: `src/test/java/com/gdschongik/gdsc/helper/scenario/`

필수 사항:
- `ScenarioAcceptanceTest` 베이스 클래스 생성 (`IntegrationTest` 상속)
- `@EnableScenarios` 어노테이션 적용
- 테스트 메서드는 `Scenario` 파라미터를 받아야 함

### Scenario API 주요 메서드

1. `stimulate(Runnable action)`
   - 테스트할 동작(이벤트 발행)을 실행
   - 일반적으로 애플리케이션 서비스 메서드 호출
   - 도메인 이벤트가 발행되는 시작점

2. `andWaitForStateChange(Supplier<S> supplier, Predicate<T> condition)`
   - 비동기 이벤트 처리가 완료될 때까지 대기
   - `supplier`: 상태를 반복적으로 조회하는 함수
   - `condition`: 대기 종료 조건

3. `andVerify(Consumer<T> verification)`
   - 최종 상태를 검증
   - AssertJ를 사용하여 엔티티 상태 확인

### 주의사항

1. 테스트 격리: 각 테스트마다 데이터베이스가 초기화됨
2. 이벤트 완료 대기: `hasEventCompleted()`를 사용하여 모든 비동기 처리가 완료될 때까지 대기
3. 스레드 풀 관리: 각 테스트마다 스레드 풀이 재초기화되어 더티 스레드 문제 방지
4. 타임아웃: 이벤트 대기는 최대 10초이므로, 긴 처리 시간이 필요한 경우 주의

---

## 테스트 상수 및 Fixture

### 테스트 상수 클래스

테스트에서 공통으로 사용되는 상수는 도메인별 상수 클래스에 정의합니다:

위치: `src/test/java/com/gdschongik/gdsc/global/common/constant/`

```java
// MemberConstant.java
public static final String OAUTH_ID = "testOauthId";
public static final String UNIV_EMAIL = "b000000@g.hongik.ac.kr";
public static final String NAME = "김홍익";
public static final String STUDENT_ID = "C123456";

// StudyConstant.java
public static final String STUDY_TITLE = "스터디 제목";
public static final Long TOTAL_WEEK = 8L;
public static final DayOfWeek DAY_OF_WEEK = DayOfWeek.FRIDAY;
```

### FixtureHelper

단위 테스트에서 엔티티를 생성할 때 사용합니다:

```java
// FixtureHelper.java
public Member createGuestMember(Long id) {
    Member member = Member.createGuest(OAUTH_ID);
    ReflectionTestUtils.setField(member, "id", id);
    return member;
}

public Member createAssociateMember(Long id) {
    Member member = createGuestMember(id);
    member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
    member.completeUnivEmailVerification(UNIV_EMAIL);
    member.verifyDiscord(DISCORD_USERNAME, NICKNAME);
    member.advanceToAssociate();
    return member;
}
```

- `ReflectionTestUtils.setField()`로 ID 설정 (영속화 없이 ID 부여)
- 단위 테스트에서만 사용 (통합 테스트는 `IntegrationTest` 헬퍼 메서드 사용)

---

## 테스트 실행 명령어

### 전체 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests MemberServiceTest

# 특정 패키지 테스트 실행
./gradlew test --tests "com.gdschongik.gdsc.domain.member.*"

# 특정 테스트 메서드 실행
./gradlew test --tests MemberServiceTest.testCreateMember

# 모든 검증 (테스트 + 포맷팅 체크)
./gradlew check
```

### 빌드 및 테스트 문제 해결

```bash
# Gradle 캐시 정리 후 재빌드
./gradlew clean build

# 상세 로그 출력
./gradlew test --debug
```

---

## 테스트 Best Practices

1. **단일 책임**: 각 테스트는 하나의 행동만 테스트
2. **명확한 이름**: 테스트 목적이 명확하게 드러나야 함
3. **격리**: 테스트 간 독립성 유지
4. **반복성**: 같은 테스트를 여러 번 실행해도 결과 동일
5. **계층 분리**: 단위 테스트와 통합 테스트의 역할을 명확히 구분
   - 단위 테스트: 비즈니스 규칙 검증
   - 통합 테스트: 서비스 간 흐름 검증

---

## 테스트 설정 클래스

### TestQuerydslConfig

Querydsl `JPAQueryFactory` 빈을 제공합니다:

```java
@TestConfiguration
public class TestQuerydslConfig {

    @Autowired
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

### TestSyncExecutorConfig

비동기 작업을 동기로 처리하기 위한 `SyncTaskExecutor`를 제공합니다.

> **참고**: `@TestConfiguration`이 아닌 `@Import`로 직접 추가하는 방식을 사용합니다.

```java
// IntegrationTest.java
@Import({TestSyncExecutorConfig.class, TestLockConfig.class})
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTest { ... }
```

### TestLockConfig

분산 락의 인메모리 구현체를 제공합니다:

```java
@TestConfiguration
public class TestLockConfig {

    @Bean
    public InmemoryLockUtil inmemoryLockUtil() {
        return new InmemoryLockUtil();
    }
}
```

### TestRedisConfig

테스트용 Redis 설정을 제공합니다:

```java
@TestConfiguration
@Import(RedisConfig.class)
public class TestRedisConfig { }
```
