package com.smallnine.apiserver.security.oauth2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores short-lived one-time auth codes that the OAuth2 success handler
 * issues in place of putting JWT tokens directly in the redirect URL.
 *
 * Code TTL is intentionally short (60s). Codes are deleted on first lookup.
 */
@Component
@RequiredArgsConstructor
public class OAuth2CodeStore {

    private static final String KEY_PREFIX = "oauth:code:";
    private static final Duration CODE_TTL = Duration.ofSeconds(60);

    private final RedisTemplate<String, Object> redisTemplate;

    public String issue(Long userId, String accessToken, String refreshToken, long accessExpiresAtEpochMilli) {
        String code = UUID.randomUUID().toString().replace("-", "");
        Payload payload = new Payload(userId, accessToken, refreshToken, accessExpiresAtEpochMilli);
        redisTemplate.opsForValue().set(KEY_PREFIX + code, payload, CODE_TTL);
        return code;
    }

    public Optional<Payload> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + code;
        Object value = redisTemplate.opsForValue().getAndDelete(key);
        if (value instanceof Payload p) {
            return Optional.of(p);
        }
        return Optional.empty();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long userId;
        private String accessToken;
        private String refreshToken;
        private long accessExpiresAtEpochMilli;
    }
}