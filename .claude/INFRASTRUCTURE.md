# INFRASTRUCTURE.md

이 문서는 gdsc-server의 배포 인프라 아키텍처, 서버 환경, 데이터베이스 구성, 모니터링 시스템을 설명합니다.

> **외부 서비스 연동**: Discord, GitHub, 토스페이먼츠, Gmail 등 외부 서비스 연동은 [EXTERNAL.md](EXTERNAL.md) 참조

---

## 전체 서비스 인프라 아키텍처

### 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                         Clients                                  │
│  ┌───────────┐ ┌───────┐ ┌───────┐ ┌────────┐ ┌───────┐        │
│  │ Onboarding│ │ Admin │ │ Study │ │ Mentor │ │ Event │        │
│  └─────┬─────┘ └───┬───┘ └───┬───┘ └────┬───┘ └───┬───┘        │
└────────┼───────────┼─────────┼──────────┼─────────┼─────────────┘
         │           │         │          │         │
         └───────────┴────┬────┴──────────┴─────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │     gdsc-server       │
              │   (Spring Boot 3.2)   │
              │        EC2            │
              └───────────┬───────────┘
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
    ┌─────────┐     ┌─────────┐    ┌──────────────┐
    │  MySQL  │     │  Redis  │    │ External APIs│
    │         │     │         │    │ (EXTERNAL.md)│
    └─────────┘     └─────────┘    └──────────────┘
```

### 서비스 구성

gdsc-server는 **단일 Spring Boot 애플리케이션**으로 구성됩니다.

| 항목 | 내용 |
|------|------|
| 서비스명 | gdsc-server |
| 프레임워크 | Spring Boot 3.2.3 |
| Java 버전 | Java 17 |
| 배포 방식 | Docker 컨테이너 + EC2 |
| 아키텍처 | linux/arm64/v8 |

### 환경 프로필

| 환경 | 설명 |
|------|------|
| `local` | 로컬 개발 환경 |
| `dev` | 개발 서버 |
| `prod` | 프로덕션 서버 |

---

## 데이터베이스 구성

### MySQL (Primary Database)

**설정 파일**: `application-datasource.yml`

| 환경 | ddl-auto |
|------|----------|
| local | update |
| dev | update |
| prod | none |

**주요 용도**:
- 전체 비즈니스 데이터 저장
- Named Lock 기반 분산 락 (상세: [LOCK.md](LOCK.md))

### Redis

**설정 파일**: `application-redis.yml`

**저장 데이터** (모두 TTL 기반):

| 데이터 | 용도 |
|--------|------|
| `DiscordVerificationCode` | Discord 인증 코드 |
| `RefreshToken` | JWT 리프레시 토큰 |
| `UnivEmailVerification` | 대학교 이메일 인증 토큰 |

---

## 서비스 간 이벤트 통신

gdsc-server는 **Spring Modulith**를 사용하여 도메인 간 비동기 이벤트 통신을 구현합니다.

**설정 파일**: `application-modulith.yml`

> 상세 내용은 [EVENT.md](EVENT.md) 참조

---

## 예외 수집 (Sentry)

**설정 파일**: `application-sentry.yml`

| 설정 | 값 |
|------|-----|
| traces-sample-rate | 1.0 |
| environment | 프로필에 따라 결정 |
| logging.minimum-event-level | warn |

### 커스텀 필터링

`CustomBeforeSendTransactionCallback`에서 actuator, swagger 등 특정 엔드포인트의 트랜잭션을 필터링합니다.

> Sentry 연동 상세 설정은 [EXTERNAL.md](EXTERNAL.md#5-sentry-연동) 참조

---

## 모니터링

### Spring Actuator

**설정 파일**: `application-actuator.yml`

| 엔드포인트 | 설명 | 인증 |
|------------|------|------|
| `/gdsc-actuator/health` | 헬스체크 | 공개 |
| `/gdsc-actuator/prometheus` | Prometheus 메트릭 | Basic Auth (prod) |

### Prometheus & Grafana

- Prometheus 메트릭 수집 (`micrometer-registry-prometheus`)
- Grafana 대시보드를 통한 모니터링

---

## 설정 파일 구조

### 인프라 설정 파일

| 설정 파일 | 설명 |
|-----------|------|
| `application.yml` | 메인 설정 (프로필 그룹) |
| `application-local.yml` | 로컬 환경 |
| `application-dev.yml` | 개발 환경 |
| `application-prod.yml` | 프로덕션 환경 |
| `application-datasource.yml` | MySQL |
| `application-redis.yml` | Redis |
| `application-actuator.yml` | Actuator |
| `application-modulith.yml` | Spring Modulith |

### 외부 서비스 설정 파일

외부 서비스 관련 설정 파일은 [EXTERNAL.md](EXTERNAL.md#6-설정-파일-구조)에서 설명합니다:

- `application-security.yml` - OAuth2 + JWT
- `application-discord.yml` - Discord 봇
- `application-github.yml` - GitHub API
- `application-payment.yml` - 토스페이먼츠
- `application-email.yml` - Gmail SMTP
- `application-sentry.yml` - Sentry

---

## 참고 문서

상위 문서:
- [CLAUDE.md](CLAUDE.md) - 프로젝트 전체 개요

관련 문서:
- [DEPLOYMENT.md](DEPLOYMENT.md) - CI/CD 파이프라인 및 배포
- [EXTERNAL.md](EXTERNAL.md) - 외부 서비스 연동 아키텍처
- [DISCORD.md](DISCORD.md) - Discord 봇 통합 아키텍처
- [LOCK.md](LOCK.md) - 분산 락 (MySQL Named Lock)
- [EVENT.md](EVENT.md) - Spring Modulith 이벤트
- [SECURITY.md](SECURITY.md) - 인증/인가 아키텍처
