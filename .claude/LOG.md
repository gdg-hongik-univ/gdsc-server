# LOG.md

이 문서는 프로젝트의 로깅 전략에 대해 설명합니다.

---

## 로깅 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────┐
│                      HTTP Request                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     LoggingFilter                            │
│  - Request Body 캐싱                                         │
│  - 로깅 제외 URL 필터링                                       │
│  - 요청 처리 시간 측정                                        │
│  - 로그 메시지 생성 및 출력                                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
│  - Controller / Service 로깅                                 │
│  - 예외 발생 시 GlobalExceptionHandler 로깅                  │
└─────────────────────────────────────────────────────────────┘
```

---

## HTTP 요청/응답 로깅

### LoggingFilter

`global/log/LoggingFilter`는 모든 HTTP 요청/응답을 필터로 가로채 로깅합니다.

**주요 기능**:
- Request Body 캐싱 (`ContentCachingRequestWrapper` 사용)
- 로깅 제외 URL 필터링
- 요청 처리 시간 측정
- 통합 로그 메시지 출력

**구현 위치**: `src/main/java/com/gdschongik/gdsc/global/log/LoggingFilter.java`

### 로그 포맷

```
[REQUEST] {method} {uri} {status} ({elapsedTime}) >> REQUEST_QUERY: {query} >> REQUEST_PARAM: {body}
```

**예시**:
```
[REQUEST] POST /api/v1/members 200 OK (0.123) >> REQUEST_QUERY: null >> REQUEST_PARAM: {"name":"test"}
[REQUEST] GET /api/v1/events?page=0 200 OK (0.045) >> REQUEST_QUERY: page=0 >> REQUEST_PARAM:
```

### 로깅 제외 URL

`UrlConstant.getLogExcludeUrlList()`에서 관리하는 URL은 로깅에서 제외됩니다:

| URL 패턴 | 제외 이유 |
|----------|-----------|
| `/gdsc-actuator/health` | 헬스체크 빈번 호출 |
| `/gdsc-actuator/prometheus` | 메트릭 수집 빈번 호출 |
| `/swagger-ui/*` | API 문서 관련 |
| `/v3/api-docs`, `/v3/api-docs/swagger-config` | OpenAPI 스펙 |

---

## 환경별 로그 설정

### logback-spring.xml

`src/main/resources/logback-spring.xml`에서 환경별 로그 설정을 관리합니다.

| 프로필 | 로그 레벨 | 추가 설정 |
|--------|-----------|-----------|
| local | INFO (root) | JPA, Transaction, Web DEBUG |
| test | INFO (root) | local과 동일 |
| dev | INFO (root) | 기본 설정 |
| prod | INFO (root) | 기본 설정 |

**local/test 환경 추가 DEBUG 로거**:
- `org.springframework.orm.jpa`
- `org.springframework.transaction`
- `org.springframework.web`

### application-*.yml 로깅 설정

| 프로필 | API 로그 | Feign 로그 | Hibernate 로그 |
|--------|----------|------------|----------------|
| local | debug | debug | SQL: debug, bind: trace |
| dev | info | debug | 기본값 |
| prod | info | 기본값 | 기본값 |

### 환경 변수 기반 로그 레벨 조정 (local 전용)

local 환경에서는 환경 변수를 통해 로그 레벨을 동적으로 조정할 수 있습니다. 로컬 개발 시 특정 영역의 디버그 로그를 활성화/비활성화할 때 유용합니다.

```yaml
# application-local.yml
logging:
  level:
    com.gdschongik.gdsc.domain.*.api.*: ${LOG_LEVEL_API:debug}
    com.gdschongik.gdsc.infra.feign: ${LOG_LEVEL_FEIGN:debug}
    org.hibernate.orm.jdbc.bind: ${LOG_LEVEL_HIBERNATE_BIND:trace}
    org.hibernate.SQL: ${LOG_LEVEL_HIBERNATE_SQL:debug}
```

**사용 예시**:
```bash
# Hibernate 바인딩 로그 비활성화
LOG_LEVEL_HIBERNATE_BIND=off ./gradlew bootRun

# API 로그만 info 레벨로 조정
LOG_LEVEL_API=info ./gradlew bootRun
```

---

## 예외 로깅

예외 발생 시 `GlobalExceptionHandler`에서 로깅을 수행합니다.

| 예외 타입 | 로그 레벨 |
|-----------|-----------|
| CustomException | INFO |
| 일반 Exception | ERROR |

예외 처리 및 로깅에 대한 상세 내용은 [EXCEPTION.md](./EXCEPTION.md)를 참조하세요.

---

## 도메인별 로깅

### Discord 예외 로깅

Discord 봇 관련 예외는 `DiscordExceptionDispatcher`를 통해 별도로 처리됩니다.

상세 내용은 [DISCORD.md](./DISCORD.md)를 참조하세요.

---

## 향후 개선 사항

- [ ] JSON 형식 로깅 지원 검토 (dev/prod 환경)
