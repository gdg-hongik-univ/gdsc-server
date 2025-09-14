package com.gdschongik.gdsc.global.config;

import static com.gdschongik.gdsc.global.common.constant.EnvironmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.SwaggerUrlConstant.*;
import static com.gdschongik.gdsc.global.common.constant.UrlConstant.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.security.config.Customizer.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdschongik.gdsc.domain.auth.application.JwtService;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.global.annotation.ConditionalOnProfile;
import com.gdschongik.gdsc.global.property.BasicAuthProperty;
import com.gdschongik.gdsc.global.security.CustomOAuth2AuthorizationRequestResolver;
import com.gdschongik.gdsc.global.security.CustomSuccessHandler;
import com.gdschongik.gdsc.global.security.CustomUserService;
import com.gdschongik.gdsc.global.security.JwtExceptionFilter;
import com.gdschongik.gdsc.global.security.JwtFilter;
import com.gdschongik.gdsc.global.util.CookieUtil;
import com.gdschongik.gdsc.global.util.EnvironmentUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper;
    private final EnvironmentUtil environmentUtil;
    private final BasicAuthProperty basicAuthProperty;
    private final ClientRegistrationRepository clientRegistrationRepository;

    private void defaultFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    @Bean
    @Order(1)
    @ConditionalOnProfile({DEV, LOCAL})
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.securityMatcher(getSwaggerUrls())
                .oauth2Login(AbstractHttpConfigurer::disable)
                .httpBasic(withDefaults());

        http.authorizeHttpRequests(
                environmentUtil.isDevProfile()
                        ? authorize -> authorize.anyRequest().authenticated()
                        : authorize -> authorize.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain basicAuthFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.securityMatcher("/webhook/**")
                .oauth2Login(AbstractHttpConfigurer::disable)
                .httpBasic(withDefaults());

        http.authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("WEBHOOK"));

        return http.build();
    }

    @Bean
    @Order(3)
    @ConditionalOnProfile(PROD)
    public SecurityFilterChain prometheusFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.securityMatcher("/gdsc-actuator/prometheus").httpBasic(withDefaults());

        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.oauth2Login(oauth2 -> oauth2.authorizationEndpoint(
                        endpoint -> endpoint.authorizationRequestResolver(customOAuth2AuthorizationRequestResolver()))
                .userInfoEndpoint(userInfo -> userInfo.userService(customUserService(memberRepository)))
                .successHandler(customSuccessHandler(jwtService, cookieUtil))
                .failureHandler((request, response, exception) -> response.setStatus(401)));

        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint((request, response, authException) -> response.setStatus(401)));

        http.addFilterAfter(new JwtExceptionFilter(objectMapper), LogoutFilter.class);
        http.addFilterAfter(new JwtFilter(jwtService, cookieUtil), LogoutFilter.class);

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/oauth2/**")
                .permitAll()
                .requestMatchers("/gdsc-actuator/**")
                .permitAll()
                .requestMatchers("/onboarding/verify-email")
                .permitAll()
                .requestMatchers("/participant/**")
                .permitAll()
                .requestMatchers("/test/**")
                .permitAll()
                .requestMatchers("/onboarding/**")
                .authenticated()
                .requestMatchers("/admin/**", "/v2/admin/**")
                .hasRole("ADMIN")
                .requestMatchers("/mentor/**", "/v2/mentor/**")
                .hasAnyRole("MENTOR", "ADMIN")
                .anyRequest()
                .authenticated());

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails user = User.withUsername(basicAuthProperty.getUsername())
                .password(passwordEncoder().encode(basicAuthProperty.getPassword()))
                .roles("SWAGGER", "WEBHOOK")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver() {
        return new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Bean
    public CustomUserService customUserService(MemberRepository memberRepository) {
        return new CustomUserService(memberRepository);
    }

    @Bean
    public CustomSuccessHandler customSuccessHandler(JwtService jwtService, CookieUtil cookieUtil) {
        return new CustomSuccessHandler(jwtService, cookieUtil);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (environmentUtil.isProdProfile()) {
            configuration.setAllowedOriginPatterns(PROD_CLIENT_URLS);
        }

        if (environmentUtil.isDevProfile()) {
            List<String> urls = new ArrayList<>();
            urls.addAll(DEV_AND_LOCAL_CLIENT_URLS);
            urls.add(DEV_SERVER_URL);
            urls.add(LOCAL_SERVER_URL);
            configuration.setAllowedOriginPatterns(urls);
        }

        if (environmentUtil.isLocalProfile()) {
            configuration.setAllowedOriginPatterns(LOCAL_CLIENT_URLS);
        }

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader(SET_COOKIE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
