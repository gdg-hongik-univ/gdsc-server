package com.gdschongik.gdsc.global.config;

import static com.gdschongik.gdsc.global.common.constant.StorageConstant.R2_REGION;

import com.gdschongik.gdsc.global.property.R2StorageProperty;
import java.net.URI;
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
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(r2StorageProperty.getAccessKeyId(), r2StorageProperty.getSecretAccessKey());

        return S3Presigner.builder()
                .endpointOverride(URI.create(r2StorageProperty.getEndpoint()))
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
}
