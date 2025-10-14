package com.gdschongik.gdsc.global.common.constant;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class UrlConstant {

    private UrlConstant() {}

    // 클라이언트 URL
    public static final String PROD_CLIENT_ONBOARDING_URL = "https://onboarding.wawoo.dev";
    public static final String PROD_CLIENT_ADMIN_URL = "https://admin.wawoo.dev";
    public static final String PROD_CLIENT_STUDY_URL = "https://study.wawoo.dev";
    public static final String PROD_CLIENT_STUDY_MENTOR_URL = "https://mentor.study.wawoo.dev";
    public static final String PROD_CLIENT_EVENT_URL = "https://event.wawoo.dev";

    public static final String DEV_CLIENT_ONBOARDING_URL = "https://dev-onboarding.wawoo.dev";
    public static final String DEV_CLIENT_ADMIN_URL = "https://dev-admin.wawoo.dev";
    public static final String DEV_CLIENT_STUDY_URL = "https://dev-study.wawoo.dev";
    public static final String DEV_CLIENT_STUDY_MENTOR_URL = "https://dev-mentor.study.wawoo.dev";
    public static final String DEV_CLIENT_EVENT_URL = "https://dev-event.wawoo.dev";

    public static final String LOCAL_CLIENT_ONBOARDING_URL = "https://local-onboarding.wawoo.dev";
    public static final String LOCAL_CLIENT_ADMIN_URL = "https://local-admin.wawoo.dev";
    public static final String LOCAL_CLIENT_STUDY_URL = "https://local-study.wawoo.dev";
    public static final String LOCAL_CLIENT_STUDY_MENTOR_URL = "https://local-mentor.study.wawoo.dev";
    public static final String LOCAL_CLIENT_EVENT_URL = "https://local-event.wawoo.dev";

    public static final String LOCAL_REACT_CLIENT_URL = "http://localhost:3000";
    public static final String LOCAL_REACT_CLIENT_SECURE_URL = "https://localhost:3000";
    public static final String LOCAL_VITE_CLIENT_URL = "http://localhost:5173";
    public static final String LOCAL_VITE_CLIENT_SECURE_URL = "https://localhost:5173";

    public static final List<String> PROD_CLIENT_URLS = List.of(
            PROD_CLIENT_ONBOARDING_URL,
            PROD_CLIENT_ADMIN_URL,
            PROD_CLIENT_STUDY_URL,
            PROD_CLIENT_STUDY_MENTOR_URL,
            PROD_CLIENT_EVENT_URL);

    public static final List<String> DEV_CLIENT_URLS = List.of(
            DEV_CLIENT_ONBOARDING_URL,
            DEV_CLIENT_ADMIN_URL,
            DEV_CLIENT_STUDY_URL,
            DEV_CLIENT_STUDY_MENTOR_URL,
            DEV_CLIENT_EVENT_URL);

    public static final List<String> LOCAL_CLIENT_URLS = List.of(
            LOCAL_CLIENT_ONBOARDING_URL,
            LOCAL_CLIENT_ADMIN_URL,
            LOCAL_CLIENT_STUDY_URL,
            LOCAL_CLIENT_STUDY_MENTOR_URL,
            LOCAL_CLIENT_EVENT_URL,
            LOCAL_REACT_CLIENT_URL,
            LOCAL_REACT_CLIENT_SECURE_URL,
            LOCAL_VITE_CLIENT_URL,
            LOCAL_VITE_CLIENT_SECURE_URL);

    public static final List<String> DEV_AND_LOCAL_CLIENT_URLS =
            Stream.concat(DEV_CLIENT_URLS.stream(), LOCAL_CLIENT_URLS.stream()).toList();

    // 서버 URL
    public static final String PROD_SERVER_URL = "https://api.wawoo.dev";
    public static final String DEV_SERVER_URL = "https://dev-api.wawoo.dev";
    public static final String LOCAL_SERVER_URL = "http://localhost:8080";

    // 기타
    public static final String ROOT_DOMAIN = "wawoo.dev";
    public static final String STUDY_ANNOUNCEMENT_DOMAIN = "https://www.gdschongik.com";

    // 로깅 제외 URL
    public static List<String> getLogExcludeUrlList() {
        return Arrays.asList(
                "/gdsc-actuator/health",
                "/gdsc-actuator/prometheus",
                "/swagger-ui/index.html",
                "/swagger-ui/favicon-32x32.png",
                "/swagger-ui/swagger-initializer.js",
                "/v3/api-docs",
                "/v3/api-docs/swagger-config",
                "/swagger-ui/swagger-ui.css",
                "/swagger-ui/index.css",
                "/swagger-ui/swagger-ui-standalone-preset.js",
                "/swagger-ui/swagger-ui-bundle.js");
    }
}
