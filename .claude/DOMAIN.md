# DOMAIN.md

이 문서는 gdsc-server 프로젝트의 도메인 로직 아키텍처, 개발 가이드, 코딩 컨벤션을 설명합니다.

---

## 도메인 계층별 코딩 컨벤션

각 도메인별 계층 구조:

```
domain/[domain]/
├── domain/      # 엔티티, 도메인 서비스, 이벤트, 검증자
├── dao/         # 저장소 (Spring Data JPA + Querydsl)
├── application/ # 애플리케이션 서비스 (@Transactional)
├── api/         # 컨트롤러 (HTTP 인터페이스)
└── dto/         # 요청/응답/내부 DTO
```

### Domain (Entity, DomainService, DomainEvent)

디렉토리 구조: `domain/[domain]/domain/`

#### 엔티티 구현

- JPA `@Entity` 어노테이션으로 데이터베이스 매핑
- PK는 `@Id` 어노테이션으로 지정, AutoIncrement 사용
- 비즈니스 로직은 최대한 엔티티에 위치하게 할 것 (풍부한 도메인 모델)
- `BaseEntity` 확장으로 감사 추적 및 도메인 이벤트 자동 관리

**BaseEntity 구조** (`domain/common/model/BaseEntity.java`):

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity extends AbstractAggregateRoot<BaseEntity> {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;
}
```

**BaseSemesterEntity** (`domain/common/model/BaseSemesterEntity.java`):

학기 정보가 필요한 엔티티(예: Study)는 `BaseSemesterEntity`를 상속합니다:

```java
public abstract class BaseSemesterEntity extends BaseEntity {
    private Integer academicYear;

    @Enumerated(EnumType.STRING)
    private SemesterType semesterType;
}
```

**엔티티 구현 예시**:

```java
@Entity
@Getter
@SQLRestriction("status='NORMAL'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 풍부한 도메인 메서드
    public void advanceToAssociate() { ... }
    public void advanceToRegular() { ... }
    private void validateStatusUpdatable() { ... }
}
```

**소프트 삭제 패턴**:

- `@SQLRestriction` 어노테이션으로 엔티티 레벨에서 소프트 삭제 필터링
- 예: `@SQLRestriction("status='NORMAL'")`

#### 도메인 서비스 구현

- `@DomainService` 어노테이션 사용
- 엔티티에 넣을 수 없는 비즈니스 로직 처리 (예: 여러 도메인에 걸친 계산, 유효성 검사)
- 저장소나 다른 서비스 의존성 금지

**위치**: `domain/[domain]/domain/service/`

```java
@DomainService
public class MemberValidator {
    public void validateMemberDemote(List<RecruitmentRound> recruitmentRounds, LocalDateTime now) {
        // 검증 로직만 포함, 저장소/서비스 의존성 없음
    }
}
```

#### 도메인 이벤트 구현

- 다른 도메인과의 의존성 또는 비동기 처리가 필요한 경우 도메인 이벤트 활용
- 이벤트는 최대한 엔티티 내부에서 발행 (`registerEvent(new DomainEvent())`)
- record 사용, 내용으로 엔티티 직접 전달보다는 PK 위주 전달
- 항상 과거형으로 네이밍 (예: `MemberAdvancedToRegularEvent`)

**이벤트 구현 예시**:

```java
public record MemberAdvancedToRegularEvent(Long memberId, String discordId) {}
```

**registerEvent 사용 예시**:

```java
public void advanceToRegular() {
    // 상태 변경 로직...
    registerEvent(new MemberAdvancedToRegularEvent(id, discordId));
}
```

> 이벤트 발행 방식, 이벤트 클래스 구조, 재시도 전략 등 자세한 내용은 [EVENT.md](EVENT.md)를 참고하세요.

---

### Dao (Repository)

디렉토리 구조: `domain/[domain]/dao/`

#### 기본 원칙

- Spring Data JPA repository를 기본으로 활용
- 복잡한 쿼리는 Custom 구현체에서 Querydsl 사용

#### 리포지토리 구조 (3계층 + QueryMethod)

1. **Repository 인터페이스** (`extends JpaRepository<T, ID>, CustomRepository`)
2. **Custom 인터페이스** (복잡한 쿼리 메서드 정의)
3. **Custom 구현체** (Querydsl/JPAQueryFactory로 쿼리 구현)
4. **QueryMethod 인터페이스** (쿼리 조건 재사용)

```java
// 1. Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    Optional<Member> findByOauthId(String oauthId);
    Optional<Member> findByStudentId(String studentId);
}

// 2. CustomRepository
public interface MemberCustomRepository {
    Page<Member> findAllByQueryOption(MemberQueryOption queryOption, Pageable pageable);
}

// 3. CustomRepositoryImpl (QueryMethod 구현)
@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository, MemberQueryMethod {
    private final JPAQueryFactory queryFactory;
    // Querydsl 쿼리 구현
}
```

#### QueryMethod 인터페이스 패턴

쿼리 조건을 default 메서드로 정의하여 여러 Repository에서 재사용합니다:

```java
public interface MemberQueryMethod {
    default BooleanExpression eqRole(MemberRole role) {
        return role != null ? member.role.eq(role) : null;
    }

    default BooleanBuilder matchesQueryOption(MemberQueryOption queryOption) {
        return new BooleanBuilder()
            .and(eqStudentId(queryOption.studentId()))
            .and(eqName(queryOption.name()));
    }
}
```

**사용 도메인**: member, order, coupon, event, study

#### 쿼리 메서드 규칙

- N+1 쿼리 문제 방지
  - fetch join 적극 활용
  - `default_batch_fetch_size: 1000` 설정 사용
- fetch join 메서드 네이밍: `findFetch`로 시작 (예: `findFetchByMemberId`)
- 대규모 결과 세트에 페이지네이션 사용
  - Spring Data Pageable & Querydsl `offset().limit()` 활용
- 조회 데이터 양이 많거나, 엔티티의 특정 컬럼만 필요한 경우 DTO 프로젝션 사용
- 암묵적 join 금지, 상황에 맞는 명시적 join 활용

**fetch join 사용 예시**:

```java
public List<Attendance> findFetchByStudyId(Long studyId) {
    return queryFactory
        .selectFrom(attendance)
        .leftJoin(attendance.studyDetail, studyDetail)
        .fetchJoin()
        .where(attendance.studyId.eq(studyId))
        .fetch();
}
```

---

### Application (Application Service, EventHandler)

디렉토리 구조: `domain/[domain]/application/`

#### 애플리케이션 서비스 구현

- 도메인 서비스와 저장소, 외부 API 호출 간 흐름 제어
- `@Transactional`으로 트랜잭션 관리, 경계 설정
- DB 확인이 필요한 입력 값 검증 (예: 요청 id 검증)

**서비스 기반 분리 패턴**:

동일 도메인 내에서 서비스별로 Service 클래스를 분리합니다. 서비스 구분:

| 서비스 | 접두사 | 대상 | Spring Security 역할 |
|--------|--------|------|----------------------|
| 와우온보딩 | `Onboarding` | 일반 사용자 | authenticated |
| 와우클래스 | `Study`, `Mentor` | 스터디 관련 | MENTOR, ADMIN |
| 와우이벤트 | `Event`, `Participant` | 이벤트 관련 | authenticated / permitAll |
| 와우어드민 | `Admin` | 관리자 | ADMIN |

- `OnboardingMemberService` - 와우온보딩 서비스용 (일반 사용자 대상)
- `AdminMemberService` - 와우어드민 서비스용 (관리자 대상, `hasRole("ADMIN")`)
- `CommonMemberService` - 내부 공용 기능

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingMemberService {
    private final MemberRepository memberRepository;
    private final MemberValidator memberValidator;  // DomainService

    @Transactional
    public void updateMemberInfo(MemberInfoRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        currentMember.updateInfo(...);
        memberRepository.save(currentMember);
    }
}
```

#### 이벤트 핸들러 구현

- `@ApplicationModuleListener`로 발행된 도메인 이벤트 청취
- 클래스 네이밍은 `[Domain]EventHandler`, 메서드 네이밍은 `handle[Event]`
- 이벤트 재시도 로직에 따라 메서드가 반복 수행될 수 있음 주의
  - 재시도가 필요한 상황이 아닌 경우 **예외 없이 로직 조용히 종료**
  - 데이터 정합성이 중요한 경우 **멱등성** 구현

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventHandler {
    private final OnboardingMemberService onboardingMemberService;

    @ApplicationModuleListener
    public void handleMemberAssociateRequirementUpdatedEvent(MemberAssociateRequirementUpdatedEvent event) {
        log.info("[MemberEventHandler] 이벤트 수신: memberId={}", event.memberId());
        onboardingMemberService.attemptAdvanceToAssociate(event.memberId());
    }
}
```

> 이벤트 처리 특성, 네이밍 컨벤션, 재시도 전략 등 자세한 내용은 [EVENT.md](EVENT.md)를 참고하세요.

---

### API (Controller)

디렉토리 구조: `domain/[domain]/api/`

#### 컨트롤러 구현

- HTTP 인터페이스 정의
- OpenAPI 주석으로 API 문서화
- HTTP 헤더 파싱
- `@Valid`로 요청 파라미터 문법적 검증

**서비스 기반 컨트롤러 분리 패턴**:

서비스와 마찬가지로 서비스별로 컨트롤러를 분리합니다:

- `OnboardingMemberController` - 와우온보딩 API (`/onboarding/**`)
- `AdminMemberController` - 와우어드민 API (`/admin/**`)
- `MentorStudyController` - 멘토용 스터디 API (`/mentor/**`)

```java
@Tag(name = "Member - Onboarding", description = "회원 온보딩 API입니다.")
@RestController
@RequestMapping("/onboarding/members")
@RequiredArgsConstructor
public class OnboardingMemberController {
    private final OnboardingMemberService onboardingMemberService;

    @Operation(summary = "내 대시보드 조회", description = "내 대시보드를 조회합니다.")
    @GetMapping("/me/dashboard")
    public ResponseEntity<MemberDashboardResponse> getDashboard() {
        return ResponseEntity.ok().body(onboardingMemberService.getDashboard());
    }

    @Operation(summary = "기본 회원정보 작성")
    @PostMapping("/me/info")
    public ResponseEntity<Void> updateMemberInfo(@Valid @RequestBody MemberInfoRequest request) {
        onboardingMemberService.updateMemberInfo(request);
        return ResponseEntity.ok().build();
    }
}
```

---

### DTO (Data Transfer Object)

디렉토리 구조:

- 요청 DTO: `domain/[domain]/dto/request/`
- 응답 DTO: `domain/[domain]/dto/response/`
- 내부 DTO: `domain/[domain]/dto/`

#### DTO 구현

- DTO 클래스는 반드시 `record`를 사용 (불변성 보장)
- 네이밍 원칙: 도메인명 + 컨트롤러 함수명
  - 예: `MemberInfoRequest`, `MemberDashboardResponse`
- 요청, 응답 본문이 중첩 구조인 경우
  - 해당 요청에서만 사용 -> 내부 record로 선언, `XXXRequest.Item`으로 네이밍
  - 여러 요청, 응답에서 중복 사용 -> dto 패키지에 별도 record로 생성

**Request DTO 예시**:

```java
public record MemberInfoRequest(
    @NotBlank @Pattern(regexp = STUDENT_ID) String studentId,
    @NotBlank String name,
    @NotBlank @Pattern(regexp = PHONE_WITHOUT_HYPHEN) String phone,
    @NotNull Department department,
    @NotBlank @Email String email
) {}
```

**Response DTO 예시**:

```java
public record MemberInfoResponse(
    Long memberId,
    String studentId,
    String name
) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(
            member.getId(),
            member.getStudentId(),
            member.getName()
        );
    }
}
```

#### 엔티티-DTO 변환 규칙

- 엔티티는 DTO에 절대 의존하지 않음 (단방향 의존성)
- 변환 방식: 별도 매퍼 클래스 없이 DTO에 `.of()`, `.from()` 팩토리 메서드 생성해 사용

---

## 예외 처리 패턴

#### 기본 규칙

- 모든 비즈니스 예외는 `new CustomException(ErrorCode)`를 사용해 생성
- 전역 예외 핸들러(`GlobalExceptionHandler`)가 자동 처리

> 생성된 예외가 어떻게 처리되는 방법에 대한 더 자세한 정보가 필요하다면 [EXCEPTION.md](EXCEPTION.md)를 참고하세요.

**예외 생성 예시**:

```java
Member member = memberRepository.findById(memberId)
    .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
```

#### ErrorCode 사용

- `ErrorCode` import 시 static 와일드카드 사용: `import static ... ErrorCode.*`
- 에러 코드 네이밍 규칙은 `도메인_에러_원인`으로 간략히 작성 (예: `MEMBER_NOT_FOUND`)

**ErrorCode 구조**:

```java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    MEMBER_NOT_FOUND(NOT_FOUND, "존재하지 않는 커뮤니티 멤버입니다."),
    MEMBER_DELETED(CONFLICT, "탈퇴한 회원입니다."),
    // ...
}
```

---

## 검증 (Validation)

검증은 단계별로 진행:

1. **컨트롤러 레벨** (문법적 검증)
   - 요청 파라미터에 `@Valid` 어노테이션 사용
   - 자료형, null 여부 등 문법적 검증
   - 검증 실패 시: 400 Bad Request 응답

2. **애플리케이션 서비스 레벨** (DB 기반 검증)
   - DB 조회 후 필요한 유효성 검증
   - 데이터베이스에서 조회하지 않으면 확인 불가능한 단순 요청 값 검증 (예: 요청 ID 존재 여부)
   - 검증 실패 시: 상황에 맞는 400대 상태 코드를 가진 `ErrorCode`로 `CustomException` 생성해 응답

3. **엔티티/도메인 서비스 레벨** (비즈니스 로직 검증)
   - 엔티티 자체 정보로만 가능한 검증: 엔티티 내부에서 진행
   - 여러 엔티티에 걸친 복잡한 검증 규칙: 도메인 서비스 활용
   - 검증 실패 시: 대부분 409 CONFLICT, DB 무결성 오류 등 시스템 문제 시 500 INTERNAL_SERVER_ERROR

---

## 개발 패턴 및 가이드

### Lombok 사용

사용 어노테이션:

- `@Getter` - getter 메서드 자동 생성
- `@Builder` - Builder 패턴 자동 생성
- `@RequiredArgsConstructor` - 필수 필드 생성자 자동 생성
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - Entity용 기본 생성자
- `@AllArgsConstructor` - DTO, VO용 전체 필드 생성자
- `@Slf4j` - SLF4J 로거 자동 주입

주의사항:

- 엔티티에서 `@Data`, `@Setter` 사용 금지
- 필요한 어노테이션만 선택적으로 사용

### 타입 안전성

Querydsl 타입 안전 쿼리:

- 문자열 기반 SQL 피하기
- Native Query 사용 금지
- 복잡한 쿼리는 Querydsl 사용

### null 안전성

Optional 사용:

- 값이 없을 수 있는 경우는 메서드의 반환값은 `Optional<T>`를 사용합니다.
- 예외:
  - 내부 성능이 중요한 루프나 대량 호출 메서드에선 사용하지 않습니다.
  - 단순한 내부 유틸, private 헬퍼 메서드 등에선 사용하지 않습니다.
  - 컬렉션의 경우 null, Optional 대신 빈 컬렉션을 반환합니다.

---

## 참고 문서

상위 문서:

- [CLAUDE.md](CLAUDE.md) - 프로젝트 전체 개요로 돌아가기

관련 문서:

- [EVENT.md](EVENT.md) - 도메인 이벤트 전략 (발행, 처리, 재시도)
- [TESTING.md](TESTING.md) - 도메인 로직 테스트 작성 가이드
- [EXCEPTION.md](EXCEPTION.md) - 예외 처리 및 공용 기능
