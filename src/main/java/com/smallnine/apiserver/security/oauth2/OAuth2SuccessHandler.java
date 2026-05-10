package com.smallnine.apiserver.security.oauth2;

import com.smallnine.apiserver.dto.GoogleProfile;
import com.smallnine.apiserver.entity.RefreshToken;
import com.smallnine.apiserver.entity.User;
import com.smallnine.apiserver.exception.BusinessException;
import com.smallnine.apiserver.exception.ConcurrentOAuthRegistrationException;
import com.smallnine.apiserver.service.MemberService;
import com.smallnine.apiserver.service.RefreshTokenService;
import com.smallnine.apiserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles successful OAuth2 login. Delegates user lookup/creation to MemberService,
 * issues JWT + refresh token, then redirects to the frontend with a one-time code
 * (NOT the tokens themselves). Frontend exchanges the code via /api/auth/oauth/exchange.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final OAuth2CodeStore codeStore;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        GoogleProfile profile = new GoogleProfile(
                oAuth2User.getAttribute("sub"),
                oAuth2User.getAttribute("email"),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("picture")
        );

        try {
            User user = resolveUser(profile);

            String accessToken = jwtUtil.generateAccessToken(user.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            long accessExpiresAt = System.currentTimeMillis() + jwtUtil.getAccessTokenExpirationTime();

            String code = codeStore.issue(user.getId(), accessToken, refreshToken.getToken(), accessExpiresAt);

            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("code", code)
                    .build()
                    .toUriString();

            log.info("action=oauth2_login user={} result=success", user.getUsername());
            response.sendRedirect(targetUrl);
        } catch (RuntimeException ex) {
            // 涵蓋：BusinessException (profile 驗證)、IllegalStateException (race recovery 失敗)、
            //       DataAccessException (DB)、RedisConnectionFailureException (Redis)、其他未預期
            // 業務例外的訊息可給 user 看；infra 例外蓋成通用訊息避免洩漏內部細節
            String userMessage = (ex instanceof BusinessException)
                    ? ex.getMessage()
                    : "Google 登入失敗，請重試";
            log.error("action=oauth2_login result=failed sub={} reason={}",
                    profile.getSub(), ex.getMessage(), ex);
            redirectToLoginError(response, userMessage);
        }
    }

    private void redirectToLoginError(HttpServletResponse response, String message) throws IOException {
        URI uri = URI.create(redirectUri);
        String errorRedirect = uri.getScheme() + "://" + uri.getAuthority()
                + "/login?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(errorRedirect);
    }

    /**
     * 處理 race condition：findOrCreateGoogleUser 撞到 google_uid UNIQUE constraint
     * 時會拋 ConcurrentOAuthRegistrationException（其 @Transactional 已 rollback），
     * 這裡用獨立 transaction 的 findByGoogleUid 取回先寫入者。
     *
     * 若 re-fetch 仍失敗（理論上不該發生：constraint 已經告訴我們有人寫入了），
     * 拋 IllegalStateException 讓上層 filter 把 user 導向錯誤頁面。
     */
    private User resolveUser(GoogleProfile profile) {
        try {
            return memberService.findOrCreateGoogleUser(profile);
        } catch (ConcurrentOAuthRegistrationException race) {
            return memberService.findByGoogleUid(race.getProviderSub())
                    .orElseThrow(() -> new IllegalStateException(
                            "OAuth2 race recovery failed; sub=" + race.getProviderSub(), race));
        }
    }
}