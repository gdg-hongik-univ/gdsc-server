package com.gdschongik.gdsc.global.config;

import static com.gdschongik.gdsc.global.common.constant.StorageConstant.*;

import com.gdschongik.gdsc.global.property.R2StorageProperty;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class R2StorageConfig {

    private final R2StorageProperty r2StorageProperty;

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner() {
        URI endpoint = parseEndpoint(r2StorageProperty.getEndpoint());
        requireText(r2StorageProperty.getBucket(), "bucket");
        requireText(r2StorageProperty.getAccessKeyId(), "access-key-id");
        requireText(r2StorageProperty.getSecretAccessKey(), "secret-access-key");
        validateUrlTtl(r2StorageProperty.getUploadUrlTtl(), "upload-url-ttl");
        validateUrlTtl(r2StorageProperty.getDownloadUrlTtl(), "download-url-ttl");

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(r2StorageProperty.getAccessKeyId(), r2StorageProperty.getSecretAccessKey());

        return S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(Region.of(R2_REGION))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(createS3Configuration())
                .build();
    }

    // 사용자 지정 S3Configuration은 checksum 검증이 기본 활성화되므로, presigner 기본값과 동일하게 명시적으로 비활성화합니다.
    @SuppressWarnings("deprecation")
    private S3Configuration createS3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .checksumValidationEnabled(false)
                .build();
    }

    private URI parseEndpoint(String endpoint) {
        requireText(endpoint, "endpoint");

        URI uri;
        try {
            uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw invalidEndpoint();
        }

        boolean isHttps = "https".equalsIgnoreCase(uri.getScheme());
        boolean hasHost = uri.getHost() != null && !uri.getHost().isBlank();
        boolean hasOnlyRootPath = uri.getRawPath() == null
                || uri.getRawPath().isEmpty()
                || uri.getRawPath().equals("/");
        boolean hasNoExtraComponent =
                uri.getRawUserInfo() == null && uri.getRawQuery() == null && uri.getRawFragment() == null;

        if (!isHttps || !hasHost || !hasOnlyRootPath || !hasNoExtraComponent) {
            throw invalidEndpoint();
        }

        try {
            return new URI("https", null, uri.getHost(), uri.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw invalidEndpoint();
        }
    }

    private void requireText(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("R2 스토리지 설정 %s%s 값이 비어 있습니다.".formatted(R2_PROPERTY_PREFIX, key));
        }
    }

    private void validateUrlTtl(Duration ttl, String key) {
        if (ttl == null
                || ttl.getNano() != 0
                || ttl.compareTo(PRESIGNED_URL_MIN_TTL) < 0
                || ttl.compareTo(PRESIGNED_URL_MAX_TTL) > 0) {
            throw new IllegalStateException(
                    "R2 스토리지 설정 %s%s 값은 1초 이상 7일 이하의 정수 초여야 합니다.".formatted(R2_PROPERTY_PREFIX, key));
        }
    }

    private IllegalStateException invalidEndpoint() {
        return new IllegalStateException("R2 스토리지 설정 %sendpoint 값은 버킷 경로, 사용자 정보, 쿼리, fragment가 없는 HTTPS URI여야 합니다."
                .formatted(R2_PROPERTY_PREFIX));
    }
}
