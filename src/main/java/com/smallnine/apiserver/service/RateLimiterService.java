package com.smallnine.apiserver.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * #H1 認證端點限流（純記憶體 token bucket）。
 *
 * 目前以 ConcurrentHashMap 持有 bucket，僅適用單機；多機 / 分散式
 * 限流（登入端點）留待 #H2 改為 Redis-backed。此處故意只覆蓋
 * resend-verification 一條路徑，不做過度設計。
 */
@Service
public class RateLimiterService {

    private final Map<String, Bucket> resendVerificationByEmail = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resendVerificationByIp = new ConcurrentHashMap<>();

    /** 每個 email：5 分鐘只准 1 次重寄 */
    private Bucket newEmailBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(5))))
                .build();
    }

    /** 每個 IP：每分鐘最多 5 次（擋整段 IP 掃信箱） */
    private Bucket newIpBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build();
    }

    /**
     * 嘗試為一次 resend-verification 請求扣 token。
     * email 與 IP 任一超限即視為被限流。
     *
     * @return true=放行；false=已超限
     */
    public boolean tryResendVerification(String email, String clientIp) {
        boolean ipOk = resendVerificationByIp
                .computeIfAbsent(clientIp, k -> newIpBucket())
                .tryConsume(1);
        if (!ipOk) {
            return false;
        }
        return resendVerificationByEmail
                .computeIfAbsent(email.toLowerCase(), k -> newEmailBucket())
                .tryConsume(1);
    }
}