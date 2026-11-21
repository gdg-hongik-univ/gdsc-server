package com.gdschongik.gdsc.infra.storage.client;

import static com.gdschongik.gdsc.global.common.constant.TestStorageConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gdschongik.gdsc.global.config.R2StorageConfig;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.property.R2StorageProperty;
import com.gdschongik.gdsc.infra.storage.dto.PresignedUrlDto;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

class R2StorageClientTest {

    private final R2StorageProperty property = new R2StorageProperty(
            R2_ENDPOINT, R2_BUCKET, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY, UPLOAD_URL_TTL, DOWNLOAD_URL_TTL);
    private final S3Presigner s3Presigner = new R2StorageConfig(property).s3Presigner();
    private final R2StorageClient r2StorageClient = new R2StorageClient(s3Presigner, property);

    @AfterEach
    void tearDown() {
        s3Presigner.close();
    }

    @Nested
    class 업로드_URL_발급시 {

        @Test
        void 도메인_이름과_UUID로_key를_만들고_Content_Type을_서명한_PUT_URL을_반환한다() {
            // given
            String keyPrefix = UPLOAD_PATH + "/";
            String expectedTtlSeconds = Long.toString(UPLOAD_URL_TTL.toSeconds());
            Instant before = Instant.now();

            // when
            PresignedUrlDto upload = r2StorageClient.generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE);
            Instant after = Instant.now();

            // then
            URI uri = URI.create(upload.url());
            assertThat(upload.objectKey()).startsWith(keyPrefix);
            String uuid = upload.objectKey().substring(keyPrefix.length());
            assertThat(UUID.fromString(uuid).toString()).isEqualTo(uuid);

            assertThat(upload.method()).isEqualTo("PUT");
            assertThat(uri.getPath()).isEqualTo("/" + R2_BUCKET + "/" + upload.objectKey());
            assertThat(UriComponentsBuilder.fromUri(uri).build(true).getQueryParams())
                    .containsEntry("X-Amz-SignedHeaders", List.of("content-type%3Bhost"))
                    .containsEntry("X-Amz-Expires", List.of(expectedTtlSeconds));
            assertThat(upload.requiredHeaders()).isEqualTo(Map.of("content-type", List.of(CONTENT_TYPE)));
            assertThat(upload.expiresAt()).isBetween(before.plus(UPLOAD_URL_TTL), after.plus(UPLOAD_URL_TTL));
        }

        @Test
        void 요청한_경로에_key를_만들고_정규화한_Content_Type을_필수_헤더로_반환한다() {
            // given
            String path = "study";
            String contentType = "Text/Plain; charset=UTF-8";

            // when
            PresignedUrlDto upload = r2StorageClient.generateUploadUrl(path, contentType);

            // then
            assertThat(upload.objectKey()).startsWith(path + "/");
            assertThat(upload.requiredHeaders()).containsEntry("content-type", List.of("text/plain;charset=UTF-8"));
        }

        @Test
        void 발급할_때마다_다른_key가_생성된다() {
            // when
            PresignedUrlDto first = r2StorageClient.generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE);
            PresignedUrlDto second = r2StorageClient.generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE);

            // then
            assertThat(first.objectKey()).isNotEqualTo(second.objectKey());
        }

        @Test
        void path가_null이면_실패한다() {
            assertInvalidPath(null);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void path가_비어있으면_실패한다(String path) {
            assertInvalidPath(path);
        }

        @Test
        void Content_Type이_null이면_실패한다() {
            assertInvalidContentType(null);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "image", "*/*", "image/*", "text/plain\n"})
        void Content_Type이_유효하지_않으면_실패한다(String contentType) {
            assertInvalidContentType(contentType);
        }

        private void assertInvalidPath(String path) {
            // when & then
            assertThatThrownBy(() -> r2StorageClient.generateUploadUrl(path, CONTENT_TYPE))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(STORAGE_INVALID_PATH.getMessage());
        }

        private void assertInvalidContentType(String contentType) {
            // when & then
            assertThatThrownBy(() -> r2StorageClient.generateUploadUrl(UPLOAD_PATH, contentType))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(STORAGE_INVALID_CONTENT_TYPE.getMessage());
        }
    }

    @Nested
    class 다운로드_URL_발급시 {

        @Test
        void 업로드시_반환된_key로_추가_헤더가_필요없는_GET_URL을_반환한다() {
            // given
            String objectKey =
                    r2StorageClient.generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE).objectKey();
            String expectedTtlSeconds = Long.toString(DOWNLOAD_URL_TTL.toSeconds());

            // when
            PresignedUrlDto download = r2StorageClient.generateDownloadUrl(objectKey);

            // then
            URI uri = URI.create(download.url());
            assertThat(download.objectKey()).isEqualTo(objectKey);
            assertThat(download.method()).isEqualTo("GET");
            assertThat(uri.getPath()).isEqualTo("/" + R2_BUCKET + "/" + objectKey);
            assertThat(UriComponentsBuilder.fromUri(uri).build(true).getQueryParams())
                    .containsEntry("X-Amz-SignedHeaders", List.of("host"))
                    .containsEntry("X-Amz-Expires", List.of(expectedTtlSeconds));
            assertThat(download.requiredHeaders()).isEmpty();
        }

        @Test
        void 한글과_URL_예약_문자가_포함된_key를_한_번만_인코딩한다() {
            // given
            String objectKey = "회원 프로필/a+b%c?d#e";

            // when
            PresignedUrlDto download = r2StorageClient.generateDownloadUrl(objectKey);

            // then
            URI uri = URI.create(download.url());
            assertThat(download.objectKey()).isEqualTo(objectKey);
            assertThat(uri.getPath()).isEqualTo("/" + R2_BUCKET + "/" + objectKey);
            assertThat(uri.getRawPath())
                    .endsWith("/%ED%9A%8C%EC%9B%90%20%ED%94%84%EB%A1%9C%ED%95%84/a%2Bb%25c%3Fd%23e");
        }

        @Test
        void key가_null이면_실패한다() {
            assertInvalidObjectKey(null);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void key가_비어있으면_실패한다(String objectKey) {
            assertInvalidObjectKey(objectKey);
        }

        private void assertInvalidObjectKey(String objectKey) {
            // when & then
            assertThatThrownBy(() -> r2StorageClient.generateDownloadUrl(objectKey))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(STORAGE_INVALID_OBJECT_KEY.getMessage());
        }
    }

    @Test
    void SDK_서명에_실패하면_서명_실패_예외로_변환한다() {
        // given
        S3Presigner failingPresigner = mock(S3Presigner.class);
        when(failingPresigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(SdkClientException.create("서명 실패"));
        R2StorageClient client = new R2StorageClient(failingPresigner, property);

        // when & then
        assertThatThrownBy(() -> client.generateDownloadUrl(UPLOAD_PATH + "/file"))
                .isInstanceOf(CustomException.class)
                .hasMessage(STORAGE_PRESIGN_FAILED.getMessage());
    }
}
