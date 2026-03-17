# DEPLOYMENT.md

이 문서는 프로젝트의 CI/CD 파이프라인, 배포 절차, 환경 구성을 설명합니다.

핵심: 빌드 및 배포는 GitHub Actions가 자동 처리합니다. 직접 배포 작업을 실행할 필요가 없습니다.

---

## 배포 개요

### 배포 전략

배포 과정:
1. 코드 커밋/푸시
2. GitHub Actions 트리거
3. 자동 빌드 및 테스트
4. Docker 이미지 생성
5. Docker Hub에 푸시
6. EC2 서버에 배포 (docker compose up)

### 배포 환경

| 환경 | 데이터베이스           | 컨테이너 | 아키텍처 | OS |
|------|------------------|----------|----------|-----|
| local | local PostgreSQL | - | - | - |
| dev | Supabase PostgreSQL | Docker Compose on EC2 | ARM64 (Graviton) | Amazon Linux |
| prod | Supabase PostgreSQL | Docker Compose on EC2 | ARM64 (Graviton) | Amazon Linux |

### 워크플로우 파일 목록

위치: `.github/workflows/`

| 파일명 | 트리거 | 역할 |
|--------|--------|------|
| `pull_request_gradle_build.yml` | PR to develop | 스타일 검사 + 테스트 |
| `pull_request_auto_assign.yml` | PR opened/reopened | PR 작성자 자동 할당 |
| `pull_request_auto_fill.yml` | PR to develop | PR 제목/본문 자동 채우기 |
| `develop_build_deploy.yml` | push to develop | 빌드 + 개발 서버 배포 |
| `develop_deploy.yml` | workflow_dispatch | 개발 서버 수동 배포 |
| `production_build_deploy.yml` | tag push (v*.*.*) | 빌드 + 프로덕션 배포 |
| `production_deploy.yml` | workflow_dispatch | 프로덕션 수동 배포 |

---

## Dev 환경 배포

### 자동 배포 (권장)

트리거: develop 브랜치에 푸시

워크플로우: `.github/workflows/develop_build_deploy.yml`

1. 빌드 및 테스트
   - GitHub Actions에서 코드 체크아웃
   - JDK 17 (temurin) 설치
   - Redis 컨테이너 실행 (테스트용)
   - `./gradlew build --configuration-cache` 실행

2. Docker 이미지 생성 및 푸시 (ARM64)
   - QEMU, Buildx로 크로스 플랫폼 빌드
   - 플랫폼: `linux/arm64/v8`
   - 이미지 태그: Git commit SHA (7자리)
   - 예시: `username/gdsc-server:abc1234`
   - Docker Hub에 푸시

3. EC2 배포
   - 배포 경로: `/home/ec2-user/dev` (Amazon Linux)
   - SCP로 docker-compose.yml 복사
   - SSH로 배포 스크립트 실행
   - `docker compose up -d`로 컨테이너 재시작

4. Slack 알림
   - 이미지 태그, Build Scan URL 포함
   - 둘기봇이 알림 발송

### 수동 배포 (롤백 용)

트리거: GitHub Actions에서 workflow_dispatch 실행

워크플로우: `.github/workflows/develop_deploy.yml`

입력값:
- `commit_hash`: 배포할 Git commit SHA (7자리)

용도:
- 특정 커밋으로 롤백이 필요할 때 사용
- 빌드 과정 없이 기존 이미지로 바로 배포

---

## Prod 환경 배포

### 자동 배포 (권장)

트리거: Semantic versioning 태그 푸시 (`v1.0.0`, `v2.1.3` 등)

워크플로우: `.github/workflows/production_build_deploy.yml`

1. 빌드 및 테스트 (Dev와 동일)

2. Docker 이미지 생성 (ARM64)
   - QEMU, Buildx로 크로스 플랫폼 빌드
   - 플랫폼: `linux/arm64/v8`
   - 이미지 태그: Semver (예: `1.2.3`)
   - `latest` 태그 미사용

3. EC2 배포
   - 배포 경로: `/home/ec2-user/prod` (Amazon Linux)
   - 나머지는 Dev와 동일

### 수동 배포 (롤백 용)

트리거: GitHub Actions에서 workflow_dispatch 실행

워크플로우: `.github/workflows/production_deploy.yml`

입력값:
- `semver`: 배포할 버전 (v 제외, 예: `1.2.3`)

용도:
- 특정 버전으로 롤백이 필요할 때 사용
- 빌드 과정 없이 기존 이미지로 바로 배포

---

## PR 검증 워크플로우

워크플로우: `.github/workflows/pull_request_gradle_build.yml`

자동 실행: develop 브랜치로의 PR 생성 또는 커밋 푸시 시

### 검증 단계

| 단계 | 설명 |
|------|------|
| 코드 체크아웃 | `actions/checkout@v4` |
| JDK 설치 | JDK 17 (temurin) |
| Gradle 검증 | `./gradlew check --configuration-cache` |

### Gradle 캐시 전략

```yaml
cache-read-only: ${{ github.ref != 'refs/heads/main' && github.ref != 'refs/heads/develop' }}
cache-encryption-key: ${{ secrets.GRADLE_CACHE_ENCRYPTION_KEY }}
```

- `main`, `develop` 브랜치: 캐시 읽기/쓰기
- feature 브랜치: 캐시 읽기 전용 (캐시 오염 방지)

### Build Scan

- PR에 Build Scan 결과를 코멘트로 자동 추가
- Gradle Build Scan URL 제공

---

## PR 자동화 워크플로우

### PR Auto Assign

워크플로우: `.github/workflows/pull_request_auto_assign.yml`

동작: PR 작성자를 자동으로 Assignee로 할당

트리거: PR `opened`, `reopened` 이벤트

설정 파일: `.github/auto_assign_config.yml`

### PR Auto Fill

워크플로우: `.github/workflows/pull_request_auto_fill.yml`

동작: 브랜치 이름에서 이슈 번호를 추출하여 PR 제목과 본문을 자동 생성

브랜치 패턴:
```
{type}/{issue_number}-{description}
```

지원 타입: `feature`, `fix`, `refactor`, `chore`, `test`, `docs`

예시:
- 브랜치: `feature/123-add-login-api`
- 이슈 제목: `:sparkles: 로그인 API 추가`
- 생성된 PR 제목: `feat: 로그인 API 추가`
- PR 본문: `close #123`

PR 제목 prefix 변환:
- `feature` → `feat:`
- 나머지 타입 → 그대로 사용 (예: `fix:`, `refactor:`)

---

## Docker 배포 구성

### Dockerfile

위치: `/Dockerfile`

```dockerfile
FROM amazoncorretto:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

- 베이스 이미지: Amazon Corretto 17
- JAR 파일 복사 방식 (멀티스테이지 빌드 미사용)
- JVM 옵션: 기본값 사용

### docker-compose.yml (배포용)

위치: `/docker-compose.yml`

```yaml
services:
  backend:
    image: ${IMAGE_FULL_URL}
    container_name: gdsc-server
    restart: always
    network_mode: host
    env_file:
      - .env
    environment:
      - TZ=Asia/Seoul
      - DOCKER_IMAGE_TAG=${IMAGE_FULL_URL}
    logging:
      driver: awslogs
      options:
        awslogs-region: "ap-northeast-2"
        awslogs-group: ${SPRING_PROFILES_ACTIVE}-server-docker-log-group
        awslogs-multiline-pattern: "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{2}:\\d{2})"
  redis:
    image: "redis:alpine"
    container_name: redis
    restart: always
    network_mode: host
```

구성 요소:
- 네트워크 모드: `host` (컨테이너가 호스트 네트워크 직접 사용)
- 환경 변수: `.env` 파일에서 로드
- 로깅: AWS CloudWatch Logs (awslogs 드라이버)
- Redis: 동일 Compose에서 함께 실행

---

## 환경 변수 및 시크릿 관리

### GitHub Secrets

| 시크릿 | 용도 |
|--------|------|
| `DOCKERHUB_USERNAME` | Docker Hub 로그인 |
| `DOCKERHUB_TOKEN` | Docker Hub 인증 |
| `EC2_HOST` | EC2 접속 주소 |
| `EC2_USERNAME` | EC2 SSH 사용자명 |
| `EC2_PRIVATE_KEY` | EC2 SSH 키 |
| `SLACK_WEBHOOK` | Slack 알림 |
| `GRADLE_CACHE_ENCRYPTION_KEY` | Gradle 캐시 암호화 |

### GitHub Environments

| 환경 | 사용 워크플로우 |
|------|-----------------|
| `develop` | develop_build_deploy, develop_deploy |
| `production` | production_build_deploy, production_deploy |

환경별로 시크릿을 분리하여 관리 (예: EC2_HOST가 환경마다 다름)

### EC2 .env 파일 관리

관리 방식:
- 노션 시크릿 문서를 통해 관리
- 개발자가 직접 EC2 SSH 접속하여 반영
- 워크플로우에서 자동 배포되지 않음 (보안)
- `.gitignore`에서 `.env`, `.env.*` 제외

포함 내용:
- `SPRING_PROFILES_ACTIVE`: 환경 프로필 (dev, prod)
- 데이터베이스 연결 정보
- 외부 서비스 API 키
- 기타 애플리케이션 설정

---

## 모니터링 및 로깅

### CloudWatch Logs

로그 수집 방식:
- Docker awslogs 드라이버 사용
- 리전: `ap-northeast-2`
- 로그 그룹: `${SPRING_PROFILES_ACTIVE}-server-docker-log-group`
  - dev 환경: `dev-server-docker-log-group`
  - prod 환경: `prod-server-docker-log-group`

멀티라인 로그 처리:
- ISO 8601 타임스탬프 기준으로 로그 라인 분리
- 스택 트레이스가 하나의 로그 항목으로 그룹화됨

### 메트릭 수집

Prometheus/Micrometer:
- `GET /actuator/prometheus`
- `GET /actuator/metrics`

> 메트릭 및 대시보드에 대한 자세한 정보는 [INFRASTRUCTURE.md](INFRASTRUCTURE.md)를 참고하세요.

### 예외 수집

- Sentry를 사용하여 예외 수집
- `@Async` 예외, 스케줄러 예외 포함

> 예외 처리에 대한 자세한 정보는 [EXCEPTION.md](EXCEPTION.md)를 참고하세요.

---

## 배포 시 주의사항

### 다운타임

- 현재 `docker compose up -d` 방식으로 다운타임이 발생할 수 있음
- 컨테이너 교체 시 짧은 순단 발생

### 롤백

- Docker Hub에서 이전 이미지를 pull하여 재배포
- 수동 배포 워크플로우 (`develop_deploy.yml`, `production_deploy.yml`) 사용
- Dev: commit hash 입력
- Prod: semver 버전 입력

### 헬스체크

- 자동 헬스체크 미구현
- 배포 후 수동으로 서비스 상태 확인 필요

---

## 참고 문서

상위 문서:
- [CLAUDE.md](CLAUDE.md) - 프로젝트 전체 개요로 돌아가기

관련 문서:
- [INFRASTRUCTURE.md](INFRASTRUCTURE.md) - 배포 인프라 아키텍처
- [EXTERNAL.md](EXTERNAL.md) - 외부 서비스 연동
- [EXCEPTION.md](EXCEPTION.md) - 예외 처리 및 Sentry 연동
