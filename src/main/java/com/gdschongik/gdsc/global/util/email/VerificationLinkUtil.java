package com.gdschongik.gdsc.global.util.email;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.VERIFY_EMAIL_REQUEST_PARAMETER_KEY;
import static com.gdschongik.gdsc.global.common.constant.EnvironmentConstant.Constants.DEV_ENV;
import static com.gdschongik.gdsc.global.common.constant.EnvironmentConstant.Constants.PROD_ENV;
import static com.gdschongik.gdsc.global.common.constant.UrlConstant.DEV_CLIENT_ONBOARDING_URL;
import static com.gdschongik.gdsc.global.common.constant.UrlConstant.LOCAL_VITE_CLIENT_SECURE_URL;
import static com.gdschongik.gdsc.global.common.constant.UrlConstant.PROD_CLIENT_ONBOARDING_URL;

import com.gdschongik.gdsc.global.util.EnvironmentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationLinkUtil {

    private final EnvironmentUtil environmentUtil;

    public String createLink(String apiEndpoint, String verificationToken) {
        String verifyEmailApiEndpoint = String.format(apiEndpoint, VERIFY_EMAIL_REQUEST_PARAMETER_KEY);
        return getClientUrl() + verifyEmailApiEndpoint + verificationToken;
    }

    private String getClientUrl() {
        return switch (environmentUtil.getCurrentProfile()) {
            case PROD_ENV -> PROD_CLIENT_ONBOARDING_URL;
            case DEV_ENV -> DEV_CLIENT_ONBOARDING_URL;
            default -> LOCAL_VITE_CLIENT_SECURE_URL;
        };
    }
}
