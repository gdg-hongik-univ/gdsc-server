package com.gdschongik.gdsc.global.config;

import static com.gdschongik.gdsc.global.common.constant.TestStorageConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.core.env.StandardEnvironment.*;

import com.gdschongik.gdsc.global.property.R2StorageProperty;
import com.gdschongik.gdsc.infra.storage.client.R2StorageClient;
import com.gdschongik.gdsc.infra.storage.dto.PresignedUrlDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

class R2StorageConfigTest {

    private static final Map<String, Object> R2_ENV = Map.of(
            "R2_ENDPOINT", "https://account.r2.cloudflarestorage.com",
            "R2_BUCKET", "dev-bucket",
            "R2_ACCESS_KEY_ID", "dev-access-key",
            "R2_SECRET_ACCESS_KEY", "dev-secret-key");

    // 실제 application.yml을 로딩하되, OS 환경 변수 대신 전달한 값만 환경 변수로 사용합니다.
    private ApplicationContextRunner contextRunner(String profile, Map<String, Object> env) {
        ApplicationContextRunner runner = new ApplicationContextRunner(() -> createContext(env))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withUserConfiguration(PropertyConfig.class, R2StorageConfig.class, R2StorageClient.class);

        return profile == null ? runner : runner.withPropertyValues("spring.profiles.active=" + profile);
    }

    private static AnnotationConfigApplicationContext createContext(Map<String, Object> env) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().remove(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment
                .getPropertySources()
                .replace(
                        SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, env));

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setEnvironment(environment);
        return context;
    }

    private static MultiValueMap<String, String> queryParams(PresignedUrlDto presignedUrl) {
        return UriComponentsBuilder.fromUriString(presignedUrl.url())
                .build(true)
                .getQueryParams();
    }

    private static void assertPresignerCreationFailed(AssertableApplicationContext context, String expectedSetting) {
        assertThat(context)
                .getFailure()
                .hasMessageContaining("s3Presigner")
                .rootCause()
                .hasMessageContaining(expectedSetting);
    }

    @Nested
    class dev_prod_프로필로_실행시 {

        @Test
        void dev는_환경_변수의_접속_정보와_기본_TTL을_사용한다() {
            assertConnectionSettingsAndDefaultTtl("dev", R2_ENV);
        }

        @Test
        void prod는_환경_변수의_접속_정보와_기본_TTL을_사용한다() {
            // given
            Map<String, Object> env = Map.of(
                    "R2_ENDPOINT", "https://prod-account.r2.cloudflarestorage.com",
                    "R2_BUCKET", "prod-bucket",
                    "R2_ACCESS_KEY_ID", "prod-access-key",
                    "R2_SECRET_ACCESS_KEY", "prod-secret-key");

            // when & then
            assertConnectionSettingsAndDefaultTtl("prod", env);
        }

        @Test
        void 업로드와_다운로드_TTL을_각각_지정할_수_있다() {
            // given
            String expectedUploadTtlSeconds = "600";
            String expectedDownloadTtlSeconds = "3600";
            Map<String, Object> env = new HashMap<>(R2_ENV);
            env.put("R2_UPLOAD_URL_TTL", "10m");
            env.put("R2_DOWNLOAD_URL_TTL", "1h");

            contextRunner("dev", env).run(context -> {
                // when
                R2StorageClient client = context.getBean(R2StorageClient.class);
                PresignedUrlDto upload = client.generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE);
                PresignedUrlDto download = client.generateDownloadUrl(upload.objectKey());

                // then
                assertThat(queryParams(upload)).containsEntry("X-Amz-Expires", List.of(expectedUploadTtlSeconds));
                assertThat(queryParams(download)).containsEntry("X-Amz-Expires", List.of(expectedDownloadTtlSeconds));
            });
        }

        @Test
        void endpoint가_누락되면_시작에_실패한다() {
            assertMissingEnvironmentVariable("R2_ENDPOINT", "endpoint override");
        }

        @Test
        void Access_Key_ID가_누락되면_시작에_실패한다() {
            assertMissingEnvironmentVariable("R2_ACCESS_KEY_ID", "Access key ID");
        }

        @Test
        void Secret_Access_Key가_누락되면_시작에_실패한다() {
            assertMissingEnvironmentVariable("R2_SECRET_ACCESS_KEY", "Secret access key");
        }

        private void assertConnectionSettingsAndDefaultTtl(String profile, Map<String, Object> env) {
            // given
            String expectedUrlPrefix = env.get("R2_ENDPOINT") + "/" + env.get("R2_BUCKET") + "/";
            String expectedCredentialPrefix = env.get("R2_ACCESS_KEY_ID") + "%2F";
            String expectedTtlSeconds = "900";

            contextRunner(profile, env).run(context -> {
                // when
                R2StorageClient client = context.getBean(R2StorageClient.class);
                PresignedUrlDto upload = client.generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE);
                PresignedUrlDto download = client.generateDownloadUrl(upload.objectKey());

                // then
                assertThat(upload.url()).startsWith(expectedUrlPrefix);
                assertThat(queryParams(upload).getFirst("X-Amz-Credential")).startsWith(expectedCredentialPrefix);
                assertThat(queryParams(upload)).containsEntry("X-Amz-Expires", List.of(expectedTtlSeconds));
                assertThat(queryParams(download)).containsEntry("X-Amz-Expires", List.of(expectedTtlSeconds));
            });
        }

        private void assertMissingEnvironmentVariable(String envName, String expectedSetting) {
            // given
            Map<String, Object> env = new HashMap<>(R2_ENV);
            env.remove(envName);

            // when & then
            contextRunner("prod", env).run(context -> assertPresignerCreationFailed(context, expectedSetting));
        }
    }

    @Nested
    class local_test_프로필로_실행시 {

        @Test
        void local은_R2_환경_변수가_있어도_더미_접속_정보로_서명한다() {
            // given
            String expectedUrlPrefix = "https://r2.local.invalid/local-bucket/";

            // when & then
            assertDummyConnectionSettings("local", expectedUrlPrefix, "local-access-key", "local-secret-key");
        }

        @Test
        void test는_R2_환경_변수가_있어도_더미_접속_정보로_서명한다() {
            // given
            String expectedUrlPrefix = R2_ENDPOINT + "/" + R2_BUCKET + "/";

            // when & then
            assertDummyConnectionSettings("test", expectedUrlPrefix, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY);
        }

        private void assertDummyConnectionSettings(
                String profile, String expectedUrlPrefix, String accessKeyId, String secretAccessKey) {
            contextRunner(profile, R2_ENV).run(context -> {
                // when
                R2StorageProperty property = context.getBean(R2StorageProperty.class);
                PresignedUrlDto upload =
                        context.getBean(R2StorageClient.class).generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE);

                // then
                assertThat(upload.url()).startsWith(expectedUrlPrefix);
                MultiValueMap<String, String> queryParams = queryParams(upload);
                assertThat(queryParams.getFirst("X-Amz-Credential")).startsWith(accessKeyId + "%2F");
                assertThat(queryParams).containsKey("X-Amz-Signature");
                assertThat(property.getSecretAccessKey()).isEqualTo(secretAccessKey);
            });
        }
    }

    @Test
    void 프로필과_R2_환경_변수가_없으면_시작에_실패한다() {
        // given
        String expectedSetting = "Access key ID";

        // when & then
        contextRunner(null, Map.of()).run(context -> assertPresignerCreationFailed(context, expectedSetting));
    }
}
