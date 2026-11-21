package com.gdschongik.gdsc.global.config;

import static com.gdschongik.gdsc.global.common.constant.TestStorageConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.core.env.StandardEnvironment.*;

import com.gdschongik.gdsc.global.property.R2StorageProperty;
import com.gdschongik.gdsc.infra.storage.client.R2StorageClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

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

    private static void assertPresignerCreationFailed(AssertableApplicationContext context) {
        assertThat(context).getFailure().hasMessageContaining("s3Presigner");
    }

    @Nested
    class dev_prod_프로필로_실행시 {

        @ParameterizedTest
        @ValueSource(strings = {"dev", "prod"})
        void 환경_변수의_접속_정보와_기본_TTL을_사용한다(String profile) {
            // given
            Duration defaultTtl = Duration.ofMinutes(15);

            contextRunner(profile, R2_ENV).run(context -> {
                // when
                R2StorageProperty property = context.getBean(R2StorageProperty.class);

                // then
                assertThat(property.getEndpoint()).isEqualTo(R2_ENV.get("R2_ENDPOINT"));
                assertThat(property.getBucket()).isEqualTo(R2_ENV.get("R2_BUCKET"));
                assertThat(property.getAccessKeyId()).isEqualTo(R2_ENV.get("R2_ACCESS_KEY_ID"));
                assertThat(property.getSecretAccessKey()).isEqualTo(R2_ENV.get("R2_SECRET_ACCESS_KEY"));
                assertThat(property.getUploadUrlTtl()).isEqualTo(defaultTtl);
                assertThat(property.getDownloadUrlTtl()).isEqualTo(defaultTtl);
            });
        }

        @Test
        void 업로드와_다운로드_TTL을_각각_지정할_수_있다() {
            // given
            Map<String, Object> env = new HashMap<>(R2_ENV);
            env.put("R2_UPLOAD_URL_TTL", "10m");
            env.put("R2_DOWNLOAD_URL_TTL", "1h");

            contextRunner("dev", env).run(context -> {
                // when
                R2StorageProperty property = context.getBean(R2StorageProperty.class);

                // then
                assertThat(property.getUploadUrlTtl()).isEqualTo(Duration.ofMinutes(10));
                assertThat(property.getDownloadUrlTtl()).isEqualTo(Duration.ofHours(1));
            });
        }

        @Test
        void 설정한_접속_정보로_R2_URL을_서명한다() {
            // given
            String expectedUrlPrefix = R2_ENV.get("R2_ENDPOINT") + "/" + R2_ENV.get("R2_BUCKET") + "/";

            contextRunner("dev", R2_ENV).run(context -> {
                // when
                R2StorageClient client = context.getBean(R2StorageClient.class);
                String url = client.generateUploadUrl(UPLOAD_PATH, CONTENT_TYPE).url();

                // then
                assertThat(url).startsWith(expectedUrlPrefix);
                var query = UriComponentsBuilder.fromUriString(url).build(true).getQueryParams();
                assertThat(UriUtils.decode(query.getFirst("X-Amz-Credential"), StandardCharsets.UTF_8))
                        .startsWith(R2_ENV.get("R2_ACCESS_KEY_ID") + "/")
                        .endsWith("/auto/s3/aws4_request");
                assertThat(query.getFirst("X-Amz-Signature")).isNotBlank();
                assertThat(query.keySet()).noneMatch(name -> name.contains("checksum"));
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"R2_ENDPOINT", "R2_ACCESS_KEY_ID", "R2_SECRET_ACCESS_KEY"})
        void 필수_접속_정보가_누락되면_시작에_실패한다(String envName) {
            // given
            Map<String, Object> env = new HashMap<>(R2_ENV);
            env.remove(envName);

            // when & then
            contextRunner("prod", env).run(R2StorageConfigTest::assertPresignerCreationFailed);
        }
    }

    @Nested
    class local_test_프로필로_실행시 {

        @Test
        void local은_R2_환경_변수가_있어도_더미_접속_정보를_사용한다() {
            assertDummyConnectionSettings(
                    "local", "https://r2.local.invalid", "local-bucket", "local-access-key", "local-secret-key");
        }

        @Test
        void test는_R2_환경_변수가_있어도_더미_접속_정보를_사용한다() {
            assertDummyConnectionSettings("test", R2_ENDPOINT, R2_BUCKET, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY);
        }

        private void assertDummyConnectionSettings(
                String profile, String endpoint, String bucket, String accessKeyId, String secretAccessKey) {
            contextRunner(profile, R2_ENV).run(context -> {
                // when
                R2StorageProperty property = context.getBean(R2StorageProperty.class);

                // then
                assertThat(property.getEndpoint()).isEqualTo(endpoint);
                assertThat(property.getBucket()).isEqualTo(bucket);
                assertThat(property.getAccessKeyId()).isEqualTo(accessKeyId);
                assertThat(property.getSecretAccessKey()).isEqualTo(secretAccessKey);
            });
        }
    }

    @Test
    void 프로필과_R2_환경_변수가_없으면_시작에_실패한다() {
        // when & then
        contextRunner(null, Map.of()).run(R2StorageConfigTest::assertPresignerCreationFailed);
    }
}
