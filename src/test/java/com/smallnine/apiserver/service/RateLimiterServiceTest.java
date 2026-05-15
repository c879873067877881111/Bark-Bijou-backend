package com.smallnine.apiserver.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * #H1 限流行為：
 *  - 每個 email 5 分鐘只准 1 次
 *  - 每個 IP 每分鐘最多 5 次
 */
class RateLimiterServiceTest {

    @Test
    void sameEmail_secondAttemptBlocked_evenFromDifferentIp() {
        RateLimiterService svc = new RateLimiterService();

        assertThat(svc.tryResendVerification("a@example.com", "1.1.1.1")).isTrue();
        assertThat(svc.tryResendVerification("a@example.com", "1.1.1.2")).isFalse();
    }

    @Test
    void sameIp_blockedAfterFiveDistinctEmails() {
        RateLimiterService svc = new RateLimiterService();
        String ip = "2.2.2.2";

        for (int i = 1; i <= 5; i++) {
            assertThat(svc.tryResendVerification("user" + i + "@example.com", ip))
                    .as("attempt %d should pass", i)
                    .isTrue();
        }
        assertThat(svc.tryResendVerification("user6@example.com", ip)).isFalse();
    }

    /** 錯誤路徑：被擋的 email 立刻重試仍然被擋（額度未回補） */
    @Test
    void blockedEmail_staysBlockedOnImmediateRetry() {
        RateLimiterService svc = new RateLimiterService();

        assertThat(svc.tryResendVerification("b@example.com", "3.3.3.3")).isTrue();
        assertThat(svc.tryResendVerification("b@example.com", "3.3.3.4")).isFalse();
        assertThat(svc.tryResendVerification("b@example.com", "3.3.3.5")).isFalse();
    }

    /** 邊界：一個 IP 用爆不應影響另一個 IP */
    @Test
    void exhaustingOneIp_doesNotAffectAnotherIp() {
        RateLimiterService svc = new RateLimiterService();
        for (int i = 1; i <= 5; i++) {
            svc.tryResendVerification("x" + i + "@example.com", "4.4.4.4");
        }
        assertThat(svc.tryResendVerification("x6@example.com", "4.4.4.4")).isFalse();
        assertThat(svc.tryResendVerification("y1@example.com", "5.5.5.5")).isTrue();
    }
}