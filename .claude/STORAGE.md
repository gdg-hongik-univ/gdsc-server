# STORAGE.md

이 문서는 gdsc-server의 Cloudflare R2 업로드 및 다운로드 presigned URL을 발급하는 공통 Bean의 설정과 사용법을 설명합니다.

---

## 개요

| 항목 | 내용 |
|------|------|
| 스토리지 | Cloudflare R2 (S3 호환 API) |
| SDK | AWS SDK for Java v2 `software.amazon.awssdk:s3` (BOM `2.55.11`) |
| 공통 Bean | `infra/storage/client/R2StorageClient` |
| 반환 DTO | `infra/storage/dto/PresignedUrlDto` |
| 설정 파일 | `application-storage.yml` (`storage.r2.*`) |
| `S3Presigner` 구성 | `global/config/R2StorageConfig` |

서버는 URL 서명만 수행하며 파일 바이트를 직접 주고받지 않습니다. 클라이언트가 발급받은 URL로 R2에 직접 PUT/GET 합니다.
명시적인 R2 자격 증명으로 로컬에서 서명하므로 URL 발급 시 R2와 네트워크 통신을 하지 않습니다.

### 책임 범위

1. 비즈니스 서비스가 파일 작업 권한을 확인합니다.
2. 서비스가 공통 Bean에 도메인 이름과 Content-Type을 전달합니다.
3. Bean이 UUID를 포함한 object key를 만들고 업로드 URL과 함께 반환합니다.
4. 서비스가 반환 DTO를 응답에 포함하고, 클라이언트가 R2에 직접 PUT 합니다.
5. 다운로드 시 서비스가 보관한 object key로 Bean을 호출하고 GET URL을 응답에 포함합니다.

Bean은 특정 도메인, 로그인 사용자, HTTP 요청 컨텍스트, DB에 의존하지 않습니다.
**파일별 권한 검사, 허용 업로드 경로, 파일 종류 및 크기 정책은 호출하는 서비스의 책임입니다.**

현재 구현에 포함하지 않는 항목은 다음과 같습니다.

- REST 컨트롤러, 기존 도메인 응답 변경, 엔티티 및 DB 변경
- 파일 메타데이터 저장, 업로드 완료 처리, 파일 존재 확인
- 삭제, 목록 조회, multipart upload
- 공개 버킷, CDN, 커스텀 도메인 구성

### 주의 사항

- URL 발급 성공은 파일 존재나 업로드 완료를 보장하지 않습니다.
- 만료되는 URL을 영구 식별자로 저장하지 마세요. **object key를 보관하고, 다운로드할 때마다 URL을 새로 발급합니다.**
- Presigned URL은 일회용이 아닙니다. 만료 전에는 같은 업로드 URL로 같은 key에 다시 쓸 수 있습니다.
- Content-Type 서명은 실제 파일 바이트의 종류나 크기를 검증하지 않습니다.

---

## 환경 설정

### YAML

`application.yml`의 `spring.profiles.include`에 `storage`가 포함되어 있으며, `application-storage.yml`에서 `storage` 프로필 활성화 조건과 공통 설정을 명시합니다.

```yaml
spring:
  config:
    activate:
      on-profile: "storage"

storage:
  r2:
    endpoint: ${R2_ENDPOINT:}
    bucket: ${R2_BUCKET:}
    access-key-id: ${R2_ACCESS_KEY_ID:}
    secret-access-key: ${R2_SECRET_ACCESS_KEY:}
    upload-url-ttl: ${R2_UPLOAD_URL_TTL:15m}
    download-url-ttl: ${R2_DOWNLOAD_URL_TTL:15m}
```

### 환경 변수

| 환경 변수 | 필수 | 의미 |
|-----------|------|------|
| `R2_ENDPOINT` | O | R2 대시보드의 S3 API endpoint. 형식은 `https://<ACCOUNT_ID>.r2.cloudflarestorage.com` |
| `R2_BUCKET` | O | 해당 환경의 버킷 이름 |
| `R2_ACCESS_KEY_ID` | O | 해당 환경의 버킷에 접근하는 R2 Access Key ID |
| `R2_SECRET_ACCESS_KEY` | O | 대응하는 R2 Secret Access Key |
| `R2_UPLOAD_URL_TTL` | X | 업로드 URL 만료 시간. 생략 시 `15m` |
| `R2_DOWNLOAD_URL_TTL` | X | 다운로드 URL 만료 시간. 생략 시 `15m` |

- local/dev/prod는 **같은 `R2_*` 변수 이름**을 사용합니다. local과 dev에는 개발용 버킷과 키, prod에는 운영용 버킷과 키를 주입합니다.
- 한 프로세스에 dev/prod 자격 증명을 함께 넣고 코드에서 고르지 않습니다. 버킷 이름과 계정 ID는 코드에 하드코딩하지 않습니다.
- endpoint에는 버킷 경로, 사용자 정보, 쿼리, fragment를 넣지 않습니다. 끝의 `/` 하나는 허용합니다.
- 공개 커스텀 도메인이나 `r2.dev` 주소가 아니라 S3 API endpoint를 사용합니다. 리전은 코드에서 `auto`로 고정합니다.
- TTL은 Spring `Duration` 형식(`30s`, `15m`, `1h`, `7d`, `PT15M` 등)으로 지정하며 **1초 이상 7일 이하의 정수 초**여야 합니다.
- TTL은 YAML(환경 변수)로만 변경하며 메서드 호출별로 바꿀 수 없습니다. 변경은 프로세스 재시작 후 새로 발급한 URL부터 적용되고, 이미 발급된 URL의 만료 시각은 바뀌지 않습니다.

### 프로필별 동작

| 프로필 | 동작 |
|--------|------|
| `local`, `dev`, `prod` | `R2_*` 환경 변수를 사용합니다. endpoint나 자격 증명이 비어 있으면 `S3Presigner` 생성에 실패하고, 버킷이나 TTL 오류는 URL 발급 또는 사용 시점에 드러날 수 있습니다. |
| `test` | `src/test/resources/application-test.yml`의 더미 접속 정보를 사용합니다. 상세 구성은 [테스트](#테스트)를 참고하세요. |
| 미지정 | `R2_*` 환경 변수를 사용합니다. 실행 시 프로필을 하나만 명시하세요. |

- local 실행 시에는 IDE 또는 실행 환경의 환경 변수 설정을 통해 개발용 `.env`의 `R2_*` 값을 애플리케이션에 주입합니다. `.env` 파일을 두는 것만으로 Spring이 자동으로 읽지는 않으므로 실행 설정에서 로딩 여부를 확인합니다.

---

## 공통 Bean 사용법

### 메서드

```java
public PresignedUrlDto generateUploadUrl(String path, String contentType);

public PresignedUrlDto generateDownloadUrl(String objectKey);
```

도메인 서비스는 `R2StorageClient`를 생성자 주입으로 사용합니다.

### 업로드 key 규칙

업로드한 객체는 도메인 폴더 바로 아래에 둡니다. 호출하는 서비스는 `path`에 `domain` 하위의 패키지 이름 하나를 전달합니다. 공통 Bean은 이름에 `/`와 새 UUID를 붙여 `{도메인명}/{UUID}` 형식의 object key를 만듭니다.

| 도메인 이름 | 결과 key |
|-----------|----------|
| `member` | `member/550e8400-e29b-41d4-a716-446655440000` |
| `study` | `study/b31174ff-1d47-4877-b47a-7f5d53aac9f9` |
| `event` | `event/7cb0419b-503a-421d-b96c-44a2d99871ee` |

다운로드는 업로드 때 반환된 object key를 그대로 받습니다. `null`과 공백뿐인 값만 거부하며 정규화, UUID 생성, 확장자 변경을 하지 않습니다.

### Content-Type 규칙

- 업로드 Content-Type은 필수이며 `image/png`처럼 구체적인 MIME 타입이어야 합니다.
- `null`, 공백, 잘못된 형식, 와일드카드(`*/*`, `image/*`, `application/*+json`), 제어 문자는 거부합니다.
- 임의 바이너리는 `application/octet-stream`을 지정합니다.
- 타입과 서브타입은 소문자로 정규화되어 서명됩니다(예: `Text/Plain; charset=UTF-8` → `text/plain;charset=UTF-8`). 클라이언트는 반환된 `requiredHeaders` 값을 그대로 사용해야 합니다.

### 반환 DTO

```java
public record PresignedUrlDto(
        String objectKey,
        String url,
        String method,
        Instant expiresAt,
        Map<String, List<String>> requiredHeaders) {}
```

| 필드 | 설명 |
|------|------|
| `objectKey` | PUT은 새로 생성한 key, GET은 입력한 key |
| `url` | SDK가 생성한 URL 문자열. host, path, query를 수정하지 않습니다. |
| `method` | `PUT` 또는 `GET` |
| `expiresAt` | SDK가 계산한 만료 시각. JSON에서 ISO 8601 문자열로 직렬화됩니다. |
| `requiredHeaders` | 요청 시 반드시 보내야 하는 서명 헤더. `host`는 브라우저가 자동으로 처리하므로 제외됩니다. PUT은 `content-type`을 포함하고 GET은 빈 map입니다. |

`requiredHeaders`의 키는 SDK 서명 결과를 따라 소문자(`content-type`)입니다. Map과 내부 List는 불변입니다.

### 예외

| ErrorCode | HTTP 상태 | 조건 |
|-----------|-----------|------|
| `STORAGE_INVALID_PATH` | 400 | 업로드 도메인 이름이 비어 있음 |
| `STORAGE_INVALID_OBJECT_KEY` | 400 | 다운로드 key 누락 또는 공백 |
| `STORAGE_INVALID_CONTENT_TYPE` | 400 | 업로드 Content-Type 누락 또는 잘못된 형식 |
| `STORAGE_PRESIGN_FAILED` | 500 | 입력 검증 이후 SDK 서명 과정에서 `SdkException` 발생 |

---

## 서비스 연동 예제

아래 코드는 사용 예시이며 실제 도메인에는 아직 적용되어 있지 않습니다. `avatarObjectKey` 등은 가상의 필드입니다.

### 기본 흐름

```java
// 서비스 생성자로 R2StorageClient를 주입받았다고 가정한다.
// 호출 전에 파일 업로드 권한을 확인한다.
PresignedUrlDto upload = r2StorageClient.generateUploadUrl("member", "image/png");

// upload를 응답 DTO에 포함하면 클라이언트가 직접 PUT할 수 있다.
// 생성된 objectKey는 향후 도메인 로직에서 별도로 보관한다.
String objectKey = upload.objectKey();

// 파일을 조회할 권한을 검사한 뒤 다운로드 URL을 발급한다.
String downloadUrl = r2StorageClient.generateDownloadUrl(objectKey).url();
```

### DTO 전체를 응답에 포함

업로드 응답에는 새로 생성된 object key와 Content-Type 사용 계약이 함께 전달되어야 하므로 DTO 전체를 포함하는 방식을 권장합니다.

```java
public record AvatarUploadUrlResponse(PresignedUrlDto upload) {}

@Service
@RequiredArgsConstructor
public class MemberAvatarService {

    private final MemberUtil memberUtil;
    private final R2StorageClient r2StorageClient;

    public AvatarUploadUrlResponse createAvatarUploadUrl(String contentType) {
        memberUtil.getCurrentMember();
        // 허용 파일 종류 등 도메인 정책은 서비스에서 검사한다.
        return new AvatarUploadUrlResponse(r2StorageClient.generateUploadUrl("member", contentType));
    }
}
```

```json
{
  "upload": {
    "objectKey": "member/550e8400-e29b-41d4-a716-446655440000",
    "url": "https://<ACCOUNT_ID>.r2.cloudflarestorage.com/<bucket>/member/550e8400-e29b-41d4-a716-446655440000?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20261004T105222Z&X-Amz-SignedHeaders=content-type%3Bhost&X-Amz-Credential=<ACCESS_KEY_ID>%2F20261004%2Fauto%2Fs3%2Faws4_request&X-Amz-Expires=900&X-Amz-Signature=<SIGNATURE>",
    "method": "PUT",
    "expiresAt": "2026-10-04T11:07:22.245154Z",
    "requiredHeaders": {
      "content-type": ["image/png"]
    }
  }
}
```

### URL 문자열만 기존 응답 필드에 포함

```java
public record MemberProfileResponse(Long memberId, String name, String avatarUrl) {

    public static MemberProfileResponse of(Member member, String avatarUrl) {
        return new MemberProfileResponse(member.getId(), member.getName(), avatarUrl);
    }
}

// 서비스: 조회 권한을 확인한 뒤 보관 중인 object key로 URL을 새로 발급한다.
String avatarUrl = r2StorageClient
        .generateDownloadUrl(member.getAvatarObjectKey())
        .url();
return MemberProfileResponse.of(member, avatarUrl);
```

```json
{
  "memberId": 123,
  "name": "김홍익",
  "avatarUrl": "https://<ACCOUNT_ID>.r2.cloudflarestorage.com/<bucket>/member/550e8400-e29b-41d4-a716-446655440000?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20261004T105222Z&X-Amz-SignedHeaders=host&X-Amz-Credential=<ACCESS_KEY_ID>%2F20261004%2Fauto%2Fs3%2Faws4_request&X-Amz-Expires=900&X-Amz-Signature=<SIGNATURE>"
}
```

---

## 클라이언트 전송 예제

- 업로드는 `multipart/form-data`가 아니라 **파일 원문 body**를 PUT 합니다.
- `requiredHeaders`의 Content-Type을 그대로 보냅니다. 다른 Content-Type으로 보내면 서명 불일치로 거부됩니다.
- 백엔드 API용 `Authorization: Bearer` 토큰을 R2 요청에 붙이지 않습니다.
- 발급된 URL을 다시 인코딩하거나 쿼리를 수정하지 않습니다.

### curl

```bash
# 업로드: 응답의 url과 requiredHeaders["content-type"] 값을 그대로 사용한다.
curl -X PUT \
  -H "Content-Type: image/png" \
  --data-binary @avatar.png \
  "$UPLOAD_URL"

# 다운로드
curl -o avatar.png "$DOWNLOAD_URL"
```

URL에 `&`가 포함되므로 반드시 따옴표로 감쌉니다.

### 브라우저 fetch

```js
// upload: 서버 응답의 PresignedUrlDto, file: <input type="file">에서 얻은 File
async function uploadToR2(upload, file) {
  const headers = Object.fromEntries(
    Object.entries(upload.requiredHeaders).map(([name, values]) => [name, values.join(", ")]),
  );

  const response = await fetch(upload.url, {
    method: upload.method, // "PUT"
    headers,
    body: file, // FormData가 아닌 File 또는 Blob
  });

  if (!response.ok) {
    throw new Error(`R2 업로드 실패: ${response.status}`);
  }

  return upload.objectKey; // 업로드 완료 처리는 서비스 API로 별도 전달한다.
}

// 다운로드: <img src={downloadUrl}>처럼 바로 사용하거나 fetch로 내려받는다.
async function downloadFromR2(downloadUrl) {
  const response = await fetch(downloadUrl);
  if (!response.ok) {
    throw new Error(`R2 다운로드 실패: ${response.status}`);
  }
  return response.blob();
}
```

브라우저에서 cross-origin으로 요청하려면 아래 [R2 CORS](#3-r2-cors-설정)가 설정되어 있어야 합니다.

### local에서 개발용 버킷 연결

local 프로필에 개발용 버킷의 접속 정보를 주입하면 실제 R2 endpoint를 사용하는 URL이 발급됩니다.

```
https://<ACCOUNT_ID>.r2.cloudflarestorage.com/<DEV_BUCKET>/member/<UUID>?X-Amz-Algorithm=AWS4-HMAC-SHA256&...&X-Amz-Signature=<SIGNATURE>
```

local에서도 발급된 URL로 개발용 버킷에 업로드·다운로드할 수 있습니다. 실제 전송은 아래 [실제 R2 확인 절차](#실제-r2-확인-절차)에 따라 dev 버킷에서 검증합니다. 브라우저에서 확인할 때는 개발용 프런트엔드 origin도 버킷 CORS에 허용해야 합니다.

---

## 비용 조건과 버킷 구성

2026년 10월 4일 기준 R2는 저장 용량과 작업 횟수로 과금하며, 버킷 개수 자체에 대한 기본료는 없습니다. [R2 요금표](https://developers.cloudflare.com/r2/pricing/)

| Standard 무료 제공량 (월) | 수량 |
|---------------------------|------|
| 저장 용량 | 10 GB-month |
| Class A 작업 (PUT, 목록 조회 등) | 100만 건 |
| Class B 작업 (GET 등) | 1,000만 건 |

- 무료 제공량은 **계정 단위**입니다. dev/prod 버킷과 같은 계정의 다른 R2 사용량을 모두 합산하며, 버킷마다 10GB가 따로 생기지 않습니다.
- Infrequent Access 저장 클래스에는 무료 제공량이 적용되지 않습니다. 저장 클래스 자동 전환이나 유료 부가 기능을 켜지 않습니다.
- 같은 계정에 dev/prod용 **비공개 Standard 버킷**을 각각 사용합니다.
- 이 기능은 사용량을 자동으로 차단하거나 과금을 방지하지 않습니다. 사용량은 Cloudflare 대시보드에서 확인합니다.

---

## 배포 준비

배포 워크플로는 dev가 `/home/ec2-user/dev/.env`, prod가 `/home/ec2-user/prod/.env`를 Compose `env_file`로 읽습니다.
CI 워크플로나 Compose 파일에 R2 변수를 따로 나열하지 않고 각 `.env`에 추가합니다.

```dotenv
# /home/ec2-user/dev/.env (prod는 같은 이름에 prod 값을 넣는다)
R2_ENDPOINT=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
R2_BUCKET=<dev 버킷 이름>
R2_ACCESS_KEY_ID=<dev 버킷 전용 Access Key ID>
R2_SECRET_ACCESS_KEY=<dev 버킷 전용 Secret Access Key>
# 필요한 경우에만 지정 (기본 15m)
# R2_UPLOAD_URL_TTL=15m
# R2_DOWNLOAD_URL_TTL=15m
```

새 이미지를 배포하기 전에 각 환경의 `.env`에 필수 R2 변수를 설정합니다.

### 1. 버킷 준비

Cloudflare 대시보드에서 dev/prod별 버킷을 생성합니다.

- 저장 클래스는 Standard를 사용합니다.
- Public access(`r2.dev` 주소, 커스텀 도메인)를 켜지 않습니다. presigned URL은 비공개 버킷에서 동작합니다.

### 2. 자격 증명 발급

R2 API 토큰을 환경별로 발급합니다.

- 권한은 **Object Read & Write**로 지정합니다.
- 적용 범위를 **해당 환경의 버킷 하나로 한정**합니다.
- 발급된 Access Key ID와 Secret Access Key, S3 API endpoint를 해당 환경의 `.env`에 넣습니다. Secret은 발급 시 한 번만 표시됩니다.

### 3. R2 CORS 설정

브라우저에서 cross-origin으로 PUT/GET 하려면 **각 R2 버킷**에 CORS 정책이 필요합니다. Spring 서버의 CORS 설정은 R2 요청에 적용되지 않습니다. [R2 CORS 문서](https://developers.cloudflare.com/r2/buckets/cors/)

```json
[
  {
    "AllowedOrigins": ["https://<프런트엔드 origin>"],
    "AllowedMethods": ["PUT", "GET"],
    "AllowedHeaders": ["Content-Type"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3600
  }
]
```

- `AllowedOrigins`에는 실제 프런트엔드 origin을 환경별로 지정합니다. `*`를 사용하지 않습니다.
- `ExposeHeaders`의 ETag는 클라이언트가 업로드 결과 ETag를 읽어야 하는 경우에만 추가합니다.

---

## 실제 R2 확인 절차

`IntegrationTest`의 R2 mock은 실제 URL 서명이나 파일 전송을 수행하지 않습니다. 실제 R2 동작은 아래 절차로 **dev 버킷에서** 확인합니다. 검증 목적으로 prod 버킷에 파일을 쓰지 않습니다.

현재는 `R2StorageClient`를 호출하는 API가 없으므로, 아래 임시 테스트로 dev 설정의 URL을 발급할 수 있습니다. 이 확인은 `IntegrationTest`를 상속하지 않고 dev 접속 정보로 실제 클라이언트와 서명기를 사용합니다. **확인 후 삭제하고 커밋하지 않습니다.**

```java
// 임시 확인용. dev R2 값을 OS 환경 변수로 주입한 뒤 실행하고, 확인 후 삭제한다.
class R2DevSmokeCheck {

    @Test
    void dev_설정으로_URL을_발급한다() {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.profiles.active=dev")
                .withUserConfiguration(PropertyConfig.class, R2StorageConfig.class, R2StorageClient.class)
                .run(context -> {
                    R2StorageClient client = context.getBean(R2StorageClient.class);
                    PresignedUrlDto upload = client.generateUploadUrl("member", "text/plain");
                    System.out.println(upload);
                    System.out.println(client.generateDownloadUrl(upload.objectKey()));
                });
    }
}
```

```bash
R2_ENDPOINT=... R2_BUCKET=<dev 버킷> R2_ACCESS_KEY_ID=... R2_SECRET_ACCESS_KEY=... \
  ./gradlew test --tests 'R2DevSmokeCheck' -i
```

출력된 URL은 만료 전까지 유효한 접근 권한이므로 공유하거나 저장하지 않습니다.

| 순서 | 확인 내용 | 기대 결과 |
|------|-----------|-----------|
| 1 | dev/prod 버킷이 비공개 Standard 버킷인지 확인 | Public access 비활성 |
| 2 | 각 API 토큰이 해당 버킷 하나에 Object Read & Write로 한정되었는지 확인 | 다른 버킷 접근 불가 |
| 3 | 버킷 CORS에 실제 프런트엔드 origin, PUT/GET, Content-Type 허용 | 브라우저 preflight 성공 |
| 4 | 작은 파일의 업로드 URL을 발급하고 반환된 Content-Type과 원문 body로 PUT | `200 OK` |
| 5 | 반환된 object key로 GET URL을 발급해 내려받은 내용과 원본 비교 | 내용 일치 |
| 6 | 실제 프런트엔드 origin에서 브라우저 fetch로 PUT/GET | 성공. curl 성공만으로 CORS를 검증했다고 판단하지 않음 |
| 7-1 | `R2_UPLOAD_URL_TTL=10s`처럼 TTL을 짧게 설정해 새 URL을 발급하고 만료 후 PUT | `403` 거부 |
| 7-2 | 유효한 업로드 URL에 다른 Content-Type으로 PUT | `403` 거부 |

7-1 확인 후에는 TTL 설정을 원래대로 되돌립니다.

---

## 테스트

### 테스트 환경 설정

test 프로필에서도 `R2StorageConfig`의 실제 `S3Presigner` Bean을 생성합니다. 클라이언트를 mock으로 교체해도 별도로 등록된 서명기는 초기화되므로, `src/test/resources/application-test.yml`에 더미 접속 정보를 둡니다.

```yaml
storage:
  r2:
    endpoint: https://r2.test.invalid
    bucket: test-bucket
    access-key-id: test-access-key
    secret-access-key: test-secret-key
    upload-url-ttl: 15m
    download-url-ttl: 15m
```

- 테스트 실행 시 R2 환경 변수를 별도로 주입할 필요가 없습니다.
- 테스트용 업로드·다운로드 TTL은 test YAML에서 각각 `15m`으로 지정합니다.
- 더미 접속 정보는 서명기 초기화용이며 실제 R2 업로드·다운로드에는 사용할 수 없습니다.

### 테스트별 Bean 구성

| 테스트 | `R2StorageClient` | `S3Presigner` |
|--------|-------------------|---------------|
| `IntegrationTest`를 상속한 도메인 테스트 | mock | 실제 Bean |
| `GdscApplicationTests` | 실제 Bean | 실제 Bean |

- 클라이언트 mock은 `IntegrationTest`의 `@MockBean` 선언으로 등록됩니다.
- `GdscApplicationTests`는 `IntegrationTest`를 상속하지 않는 독립적인 기동 테스트입니다.

이 테스트들은 실제 R2 인증이나 버킷 권한을 검증하지 않습니다. 실제 연동은 [실제 R2 확인 절차](#실제-r2-확인-절차)에 따라 dev 버킷에서 확인합니다.

> 도메인 테스트의 mock 사용법은 [TESTING.md의 외부 서비스 모킹](TESTING.md#외부-서비스-모킹)을 참고하세요.
