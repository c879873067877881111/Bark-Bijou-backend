package com.smallnine.apiserver.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 只負責 OAuth2 失敗 handler 的配置。成功 handler 已抽出為
 * {@link com.smallnine.apiserver.security.oauth2.OAuth2SuccessHandler @Component}。
 */
@Configuration
@Slf4j
public class OAuth2Config {

    @Bean
    public AuthenticationFailureHandler oAuth2AuthenticationFailureHandler(
            @Value("${app.oauth2.redirect-uri}") String redirectUri) {

        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            log.error("action=oauth2_login result=failed reason={}", exception.getMessage());
            URI uri = URI.create(redirectUri);
            String errorRedirect = uri.getScheme() + "://" + uri.getAuthority()
                    + "/login?error=" + URLEncoder.encode("Google 登入失敗", StandardCharsets.UTF_8);
            response.sendRedirect(errorRedirect);
        };
    }
}
