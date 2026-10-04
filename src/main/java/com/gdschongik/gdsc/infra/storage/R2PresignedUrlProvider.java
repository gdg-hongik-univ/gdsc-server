package com.gdschongik.gdsc.infra.storage;

import static com.gdschongik.gdsc.global.common.constant.StorageConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.property.R2StorageProperty;
import com.gdschongik.gdsc.infra.storage.dto.PresignedUrlDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.awscore.presigner.PresignedRequest;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class R2PresignedUrlProvider {

    private final S3Presigner s3Presigner;
    private final R2StorageProperty r2StorageProperty;

    /**
     * 전달받은 경로 하위에 UUID로 새 object key를 생성하고, 해당 key에 대한 업로드(PUT) URL을 발급합니다.
     * 클라이언트는 반환된 requiredHeaders의 Content-Type으로 파일 원문을 PUT 해야 합니다.
     */
    public PresignedUrlDto generateUploadUrl(String path, String contentType) {
        String objectKey = createObjectKey(path);
        String validContentType = normalizeContentType(contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2StorageProperty.getBucket())
                .key(objectKey)
                .contentType(validContentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(r2StorageProperty.getUploadUrlTtl())
                .putObjectRequest(putObjectRequest)
                .build();

        return toPresignedUrlDto(objectKey, presign(() -> s3Presigner.presignPutObject(presignRequest)));
    }

    /**
     * 업로드 URL 발급 시 반환된 object key를 그대로 사용하여 다운로드(GET) URL을 발급합니다.
     */
    public PresignedUrlDto generateDownloadUrl(String objectKey) {
        validateObjectKey(objectKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(r2StorageProperty.getBucket())
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(r2StorageProperty.getDownloadUrlTtl())
                .getObjectRequest(getObjectRequest)
                .build();

        return toPresignedUrlDto(objectKey, presign(() -> s3Presigner.presignGetObject(presignRequest)));
    }

    private String createObjectKey(String path) {
        String normalizedPath = normalizePath(path);
        String uuid = UUID.randomUUID().toString();

        if (normalizedPath.isEmpty()) {
            return uuid;
        }

        return normalizedPath + OBJECT_KEY_DELIMITER + uuid;
    }

    private String normalizePath(String path) {
        if (path == null || path.chars().anyMatch(this::isInvalidPathCharacter)) {
            throw new CustomException(STORAGE_INVALID_PATH);
        }

        String normalizedPath = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(path, '/'), '/');

        if (normalizedPath.isEmpty()) {
            return normalizedPath;
        }

        for (String segment : normalizedPath.split(OBJECT_KEY_DELIMITER, -1)) {
            if (segment.isBlank() || segment.equals(".") || segment.equals("..")) {
                throw new CustomException(STORAGE_INVALID_PATH);
            }
        }

        return normalizedPath;
    }

    private boolean isInvalidPathCharacter(int character) {
        return character == '\\' || Character.isISOControl(character);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank() || contentType.chars().anyMatch(Character::isISOControl)) {
            throw new CustomException(STORAGE_INVALID_CONTENT_TYPE);
        }

        MimeType mimeType;
        try {
            mimeType = MimeTypeUtils.parseMimeType(contentType);
        } catch (InvalidMimeTypeException e) {
            throw new CustomException(STORAGE_INVALID_CONTENT_TYPE);
        }

        if (mimeType.isWildcardType() || mimeType.isWildcardSubtype()) {
            throw new CustomException(STORAGE_INVALID_CONTENT_TYPE);
        }

        return mimeType.toString();
    }

    private void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new CustomException(STORAGE_INVALID_OBJECT_KEY);
        }
    }

    private <T extends PresignedRequest> T presign(Supplier<T> presignAction) {
        try {
            return presignAction.get();
        } catch (SdkException e) {
            log.error("R2 presigned URL 서명에 실패했습니다.", e);
            throw new CustomException(STORAGE_PRESIGN_FAILED);
        }
    }

    private PresignedUrlDto toPresignedUrlDto(String objectKey, PresignedRequest presignedRequest) {
        Map<String, List<String>> requiredHeaders = presignedRequest.signedHeaders().entrySet().stream()
                .filter(header -> !HttpHeaders.HOST.equalsIgnoreCase(header.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new PresignedUrlDto(
                objectKey,
                presignedRequest.url().toString(),
                presignedRequest.httpRequest().method().name(),
                presignedRequest.expiration(),
                requiredHeaders);
    }
}
