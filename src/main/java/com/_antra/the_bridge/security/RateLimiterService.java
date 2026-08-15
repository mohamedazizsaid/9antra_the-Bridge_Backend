package com._antra.the_bridge.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    @Autowired(required = false)
    private ProxyManager<String> proxyManager;

    // Fallback in-memory map if Redis is unavailable
    private final Map<String, Bucket> localFallbackCache = new ConcurrentHashMap<>();

    @Value("${rate.limit.global.per-min:100}")
    private long globalLimitPerMin = 100;

    @Value("${rate.limit.login.per-min:5}")
    private long loginLimitPerMin = 5;

    @Value("${rate.limit.register.per-10min:3}")
    private long registerLimitPer10Min = 3;

    @Value("${rate.limit.password-reset.per-15min:3}")
    private long passwordResetLimitPer15Min = 3;

    @Value("${rate.limit.expensive.per-min:10}")
    private long expensiveLimitPerMin = 10;

    @Value("${rate.limit.authenticated.per-min:120}")
    private long authenticatedLimitPerMin = 120;

    public enum PolicyType {
        GLOBAL,
        LOGIN,
        REGISTER,
        PASSWORD_RESET,
        EXPENSIVE,
        AUTHENTICATED
    }

    public ConsumptionProbe tryConsume(String key, PolicyType policyType) {
        Supplier<BucketConfiguration> configSupplier = () -> getBucketConfiguration(policyType);

        if (proxyManager != null) {
            try {
                Bucket bucket = proxyManager.builder().build(key, configSupplier);
                return bucket.tryConsumeAndReturnRemaining(1);
            } catch (Exception e) {
                log.warn("Redis error during rate limit check for key {}: {}. Falling back to local in-memory bucket.", key, e.getMessage());
            }
        }

        // Local fallback if Redis fails or is unconfigured
        Bucket localBucket = localFallbackCache.computeIfAbsent(key, k -> {
            BucketConfiguration config = configSupplier.get();
            return Bucket.builder().addLimit(config.getBandwidths()[0]).build();
        });
        return localBucket.tryConsumeAndReturnRemaining(1);
    }

    private BucketConfiguration getBucketConfiguration(PolicyType policyType) {
        Bandwidth bandwidth;

        switch (policyType) {
            case LOGIN:
                bandwidth = Bandwidth.builder()
                        .capacity(loginLimitPerMin)
                        .refillGreedy(loginLimitPerMin, Duration.ofMinutes(1))
                        .build();
                break;
            case REGISTER:
                bandwidth = Bandwidth.builder()
                        .capacity(registerLimitPer10Min)
                        .refillGreedy(registerLimitPer10Min, Duration.ofMinutes(10))
                        .build();
                break;
            case PASSWORD_RESET:
                bandwidth = Bandwidth.builder()
                        .capacity(passwordResetLimitPer15Min)
                        .refillGreedy(passwordResetLimitPer15Min, Duration.ofMinutes(15))
                        .build();
                break;
            case EXPENSIVE:
                bandwidth = Bandwidth.builder()
                        .capacity(expensiveLimitPerMin)
                        .refillGreedy(expensiveLimitPerMin, Duration.ofMinutes(1))
                        .build();
                break;
            case AUTHENTICATED:
                bandwidth = Bandwidth.builder()
                        .capacity(authenticatedLimitPerMin)
                        .refillGreedy(authenticatedLimitPerMin, Duration.ofMinutes(1))
                        .build();
                break;
            case GLOBAL:
            default:
                bandwidth = Bandwidth.builder()
                        .capacity(globalLimitPerMin)
                        .refillGreedy(globalLimitPerMin, Duration.ofMinutes(1))
                        .build();
                break;
        }

        return BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
    }
}
