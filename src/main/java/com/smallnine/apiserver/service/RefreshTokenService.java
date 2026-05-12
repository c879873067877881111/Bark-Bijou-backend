package com.smallnine.apiserver.service;

import com.smallnine.apiserver.entity.RefreshToken;
import com.smallnine.apiserver.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    /**
     * 為使用者發新的 refresh token。
     *
     * <p>採 token rotation 策略,不再「登入即清光該 user 全部裝置」:
     * <ul>
     *   <li>{@code rotateFromToken == null}:純發新顆,不撤銷任何舊 token
     *       (login / register / OAuth 第一次發 token 走這條,允許多裝置共存)</li>
     *   <li>{@code rotateFromToken != null}:只撤銷指定那一顆,並發新顆
     *       (refresh 流程走這條,實現 OAuth 標準 refresh token rotation)</li>
     * </ul>
     */
    RefreshToken createRefreshToken(User user, String rotateFromToken);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    /** 主動撤銷該 user 全部裝置的 refresh token (admin 強制登出 / 改密碼後使用) */
    void revokeByUser(User user);

    void revokeByToken(String token);

    void deleteExpiredTokens();
}