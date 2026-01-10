# CLAUDE.md

gdsc-server 프로젝트의 Claude Code 가이드입니다.

---

## Overview

### About

GDSC Hongik 커뮤니티 플랫폼 '와우플랫폼'의 백엔드 서버입니다.

- **와우온보딩**: 회원 가입, 정보 입력
- **와우클래스**: 스터디 관리 (멘토, 어드민)
- **와우이벤트**: 이벤트 참여, 뒤풀이 관리
- **와우어드민**: 관리자 기능

### Tech Stack

- Java 17 + Spring Boot 3.2.3 + Gradle
- MySQL + Spring Data JPA + QueryDSL
- Spring Modulith 기반 Event-Driven Development
- Spring Security 기반 JWT 인증/인가 + OAuth Social Login

---

## Build & Verify

```bash
# 전체 빌드 (테스트 + 코드 포맷 검증)
./gradlew check

# 코드 포맷팅 (Spotless)
./gradlew spotlessApply

# 빌드만 실행 (테스트 제외)
./gradlew build -x test

# 테스트 실행
./gradlew test
```

---

## Git Convention

### Branch Naming

```
type/issue-number-description
```

**타입**:
- `feature` - 기능 추가 / 변경 / 제거
- `refactor` - 기능에 영향 없는 내부 로직 변경
- `fix` - 버그 수정
- `chore` - 기능과 관련 없는 작업 (설정, 빌드, CI)
- `docs` - 문서화
- `test` - 테스트

**예시**:
```
feature/1185-block-edit-when-participation-exists
fix/983-discord-exception-handler-ignored
```

### Commit Message

- 커밋 제목만 작성하며, **절대** 커밋 본문을 작성하지 말 것
- 의도와 변경내용이 드러나도록 명확하게 작성 

```
type: description-in-korean
```

**Example**:
```
feat: 뒤풀이 참석자 조회 시 정렬 기능 구현
refactor: 이벤트 조회 API를 어드민용 및 참가자용으로 분리
fix: 과제 스터디에서 출석 마감 조회로 인한 NPE 수정
```

---

## Related Docs

- **항상** 수행하려는 작업과 관련된 문서를 아래에서 확인하세요.  

### Architecture & Dev Guide & Convention

| 문서 | 설명 |
|------|------|
| [DOMAIN.md](./DOMAIN.md) | 도메인 로직 아키텍처, 계층별 코딩 컨벤션 |
| [EXCEPTION.md](./EXCEPTION.md) | 예외 처리 전략 |
| [EVENT.md](./EVENT.md) | 도메인 이벤트 전략 |
| [TESTING.md](./TESTING.md) | 테스트 구조 및 작성 가이드 |

### Auth & Security

| 문서 | 설명 |
|------|------|
| [SECURITY.md](./SECURITY.md) | JWT 인증 아키텍처 |

### Infra & Deploy

| 문서 | 설명 |
|------|------|
| [INFRASTRUCTURE.md](./INFRASTRUCTURE.md) | 배포 인프라 (서버, DB, 모니터링) |
| [DEPLOYMENT.md](./DEPLOYMENT.md) | CI/CD 파이프라인 |

### Ops

| 문서 | 설명 |
|------|------|
| [LOG.md](./LOG.md) | HTTP 로깅, MDC |
| [LOCK.md](./LOCK.md) | 분산 락 제어 |

### External

| 문서 | 설명 |
|------|------|
| [DISCORD.md](./DISCORD.md) | Discord 봇 통합 |
| [EXTERNAL.md](./EXTERNAL.md) | 외부 서비스 연동 (GitHub, 토스페이먼츠, Email) |
