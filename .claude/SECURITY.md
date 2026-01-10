# SECURITY.md

이 문서는 gdsc-server의 인증 및 보안 아키텍처에 대해 설명합니다.

---

## 개요

gdsc-server는 **OAuth2 (GitHub) + JWT** 기반 인증을 사용합니다.

- **인증 방식**: GitHub OAuth2 로그인 후 JWT 발급
- **토큰 타입**: Access Token, Refresh Token
- **토큰 전달**: Cookie 기반 (Swagger 테스트용 Header 지원)
- **역할 체계**: 3종 역할 (MemberRole, MemberManageRole, MemberStudyRole)

---

## OAuth2 인증 흐름

### 인증 프로세스

```
[클라이언트]
    │
    │ GET /oauth2/authorization/github
    ↓
[CustomOAuth2AuthorizationRequestResolver]
    - Referer 헤더에서 target URL 추출
    - redirectUri에 target 파라미터 추가
    │
    ↓
[GitHub OAuth2 인증]
    │
    ↓
[CustomUserService.loadUser()]
    - oauthId로 기존 회원 조회
    - 없으면 Member.createGuest() 호출하여 GUEST 생성
    - lastLoginAt 업데이트
    - CustomOAuth2User 반환
    │
    ↓
[CustomSuccessHandler.onAuthenticationSuccess()]
    - MemberAuthInfo 생성
    - Access Token, Refresh Token 생성
    - 쿠키에 토큰 저장
    - target URL로 리다이렉트 (+ /social-login/redirect 경로 추가)
```

### 관련 파일

| 파일 | 역할 |
|------|------|
| `global/config/WebSecurityConfig.java` | OAuth2 설정 |
| `global/security/CustomOAuth2AuthorizationRequestResolver.java` | Referer 기반 리다이렉트 URL 처리 |
| `global/security/CustomUserService.java` | 회원 조회/생성 및 로그인 시간 업데이트 |
| `global/security/CustomSuccessHandler.java` | JWT 발급 및 쿠키 저장, 리다이렉트 처리 |
| `global/security/CustomOAuth2User.java` | OAuth2User 래퍼 (MemberAuthInfo 포함) |

### 신규 회원 등록

신규 회원은 `Member.createGuest(oauthId)`로 자동 생성됩니다.

- 기본 역할: `MemberRole.GUEST`, `MemberManageRole.NONE`, `MemberStudyRole.STUDENT`
- 상태: `MemberStatus.NORMAL`

---

## JWT 토큰 구조

### Access Token

| 항목 | 내용 |
|------|------|
| **DTO** | `AccessTokenDto(MemberAuthInfo authInfo, String tokenValue)` |
| **저장 위치** | 쿠키 (+ 헤더 지원) |

**클레임**:
- `sub` (subject): memberId
- `role`: MemberRole (GUEST, ASSOCIATE, REGULAR)
- `manageRole`: MemberManageRole (ADMIN, NONE)
- `studyRole`: MemberStudyRole (MENTOR, STUDENT)
- `iat` (issuedAt): 발급 시간
- `exp` (expiration): 만료 시간
- `iss` (issuer): 발급자

### Refresh Token

| 항목 | 내용 |
|------|------|
| **DTO** | `RefreshTokenDto(Long memberId, String tokenValue, Long ttl)` |
| **저장 위치** | 쿠키 (클라이언트) + Redis (서버) |

**클레임**:
- `sub` (subject): memberId
- `iat` (issuedAt): 발급 시간
- `exp` (expiration): 만료 시간
- `iss` (issuer): 발급자

**Redis 엔티티**:
```java
@RedisHash(value = "refreshToken")
public class RefreshToken {
    @Id
    private Long memberId;
    private String token;
    @TimeToLive
    private long ttl;
}
```

### 토큰 재발급 (RTR 방식)

Access Token 만료 시 Refresh Token이 유효하면 **RTR(Refresh Token Rotation)** 방식으로 두 토큰을 모두 재발급합니다.

- Access Token과 Refresh Token 모두 새로 발급
- Refresh Token은 Redis에 새로 저장 (기존 토큰 덮어쓰기)

### 서명 키 관리

- 토큰 타입별 별도 비밀키 사용
- HMAC-SHA 알고리즘 사용
- issuer 검증 활성화

---

## 역할 (Role) 체계

gdsc-server는 3종 역할 체계를 사용합니다.

### MemberRole (멤버십 역할)

| 값 | 권한 문자열 | 설명 |
|----|-------------|------|
| `GUEST` | `ROLE_GUEST` | 가입 대기 (온보딩 미완료) |
| `ASSOCIATE` | `ROLE_ASSOCIATE` | 준회원 |
| `REGULAR` | `ROLE_REGULAR` | 정회원 |

### MemberManageRole (관리 역할)

| 값 | 권한 문자열 | 설명 |
|----|-------------|------|
| `ADMIN` | `ROLE_ADMIN` | 관리자 |
| `NONE` | `ROLE_NONE` | 일반 (관리 권한 없음) |

### MemberStudyRole (스터디 역할)

| 값 | 권한 문자열 | 설명 |
|----|-------------|------|
| `MENTOR` | `ROLE_MENTOR` | 멘토 |
| `STUDENT` | `ROLE_STUDENT` | 학생 |

### PrincipalDetails 권한 생성

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(role.getValue()));       // ROLE_GUEST 등
    authorities.add(new SimpleGrantedAuthority(manageRole.getValue())); // ROLE_ADMIN 등
    authorities.add(new SimpleGrantedAuthority(studyRole.getValue()));  // ROLE_MENTOR 등
    return authorities;
}
```

3개의 역할이 모두 `GrantedAuthority`로 변환되어 Spring Security 권한 검증에 사용됩니다.

---

## Spring Security 필터 체인

### 다중 SecurityFilterChain 구성

| 순서 | Bean | 조건 | 매칭 패턴 | 인증 방식 |
|------|------|------|-----------|-----------|
| 1 | `swaggerFilterChain` | DEV, LOCAL | `/swagger-resources/**`, `/swagger-ui/**`, `/v3/api-docs/**` | Basic Auth (dev만 인증 필요) |
| 2 | `basicAuthFilterChain` | 항상 | `/webhook/**` | Basic Auth (WEBHOOK 역할 필요) |
| 3 | `prometheusFilterChain` | PROD | `/gdsc-actuator/prometheus` | Basic Auth |
| 4 | `filterChain` | 항상 | 그 외 전체 | JWT + OAuth2 |

### 기본 필터 설정 (모든 체인 공통)

```java
http.httpBasic(AbstractHttpConfigurer::disable)
    .formLogin(AbstractHttpConfigurer::disable)
    .logout(AbstractHttpConfigurer::disable)
    .csrf(AbstractHttpConfigurer::disable)
    .cors(withDefaults())
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

### JWT 필터 체인 순서

```
[LogoutFilter]
    ↓
[JwtExceptionFilter] - CustomException 처리
    ↓
[JwtFilter] - JWT 토큰 검증 및 인증 처리
    ↓
[이후 필터들]
```

### JwtExceptionFilter

- JWT 검증 중 발생한 `CustomException` 처리
- 예외 발생 시 적절한 에러 응답 반환

### JwtFilter

JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정합니다.

**동작 흐름**:
1. **헤더에서 AT 추출** (Swagger 테스트용, 우선 검증)
2. **쿠키에서 AT/RT 추출**
3. **AT가 유효하면** 인증 설정
4. **AT 만료 + RT 유효 시** AT/RT 재발급 후 인증 설정

---

## 공개 엔드포인트

### 인증 없이 접근 가능

| 패턴 | 용도 |
|------|------|
| `/oauth2/**` | OAuth2 인증 관련 |
| `/gdsc-actuator/**` | 헬스 체크 (Actuator) |
| `/onboarding/verify-email` | 이메일 인증 (비로그인 상태에서 접근) |
| `/participant/**` | 참가자 공개 API |
| `/test/**` | 테스트용 |

### 인증 필요 (권한 무관)

| 패턴 | 용도 |
|------|------|
| `/onboarding/**` | 온보딩 API (인증된 사용자면 모든 역할 접근 가능) |

---

## 권한 검증 (AuthorizeHttpRequests)

| 패턴 | 필요 역할 | 설명 |
|------|-----------|------|
| `/admin/**`, `/v2/admin/**` | `ROLE_ADMIN` | 관리자 전용 |
| `/mentor/**`, `/v2/mentor/**` | `ROLE_MENTOR` 또는 `ROLE_ADMIN` | 멘토 전용 (관리자 포함) |
| 그 외 모든 요청 | 인증된 사용자 | 역할 무관 |

---

## 토큰 저장 및 전달 방식

### 쿠키 기반 전달 (기본)

**쿠키 이름**:
- Access Token: `accessToken`
- Refresh Token: `refreshToken`

**쿠키 속성**:

| 속성 | 값 | 설명 |
|------|-----|------|
| path | `/` | 모든 경로에서 쿠키 전송 |
| secure | `true` | HTTPS에서만 전송 |
| sameSite | `LAX` | GET 요청 시 서드파티 쿠키 허용 |
| domain | `wawoo.dev` | 서브도메인 간 쿠키 공유 |
| httpOnly | `true` | JavaScript 접근 차단 |

### 헤더 기반 전달 (Swagger 테스트용)

- 형식: `Authorization: Bearer {accessToken}`
- 쿠키보다 헤더가 우선 검증됨
- Swagger UI에서 토큰 테스트 시 사용

---

## 로그아웃 처리

### 로그아웃 API

- **엔드포인트**: `GET /auth/logout`
- **동작**: Access Token, Refresh Token 쿠키 삭제 (maxAge=0)

### 주의사항

- 로그아웃 시 Redis의 Refresh Token은 삭제되지 않음 (쿠키만 만료)
- Refresh Token은 TTL에 의해 자연 만료됨

---

## CORS 설정

### 환경별 허용 Origin

| 환경 | 허용 Origin |
|------|-------------|
| **prod** | `https://onboarding.wawoo.dev`, `https://admin.wawoo.dev`, `https://study.wawoo.dev`, `https://mentor.study.wawoo.dev`, `https://event.wawoo.dev` |
| **dev** | DEV_CLIENT_URLS + LOCAL_CLIENT_URLS + 서버 URL |
| **local** | LOCAL_CLIENT_URLS (dev-*, local-*, localhost) |

### 공통 설정

- 모든 헤더 허용 (`*`)
- 모든 메서드 허용 (`*`)
- credentials 허용 (`true`)
- SET_COOKIE 헤더 노출

---

## Basic Auth 설정

### InMemoryUserDetailsManager

```java
UserDetails user = User.withUsername(basicAuthProperty.getUsername())
    .password(passwordEncoder().encode(basicAuthProperty.getPassword()))
    .roles("SWAGGER", "WEBHOOK")
    .build();
```

### 사용처

| 용도 | 필터 체인 | 매칭 패턴 | 역할 |
|------|-----------|-----------|------|
| Swagger UI | `swaggerFilterChain` | `/swagger-*/**`, `/v3/api-docs/**` | SWAGGER |
| Webhook | `basicAuthFilterChain` | `/webhook/**` | WEBHOOK |
| Prometheus | `prometheusFilterChain` | `/gdsc-actuator/prometheus` | (인증만 필요) |
