package com._antra.the_bridge.security;

import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    void tryConsume_loginPolicy_allowsConfiguredAttempts() {
        String key = "test:login:ip:192.168.1.1";

        // Login policy allows 5 attempts
        for (int i = 0; i < 5; i++) {
            ConsumptionProbe probe = rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.LOGIN);
            assertTrue(probe.isConsumed(), "Attempt " + (i + 1) + " should be allowed");
        }

        // 6th attempt should be blocked
        ConsumptionProbe exceededProbe = rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.LOGIN);
        assertFalse(exceededProbe.isConsumed(), "6th login attempt within a minute should be blocked");
    }

    @Test
    void tryConsume_registerPolicy_allowsConfiguredAttempts() {
        String key = "test:register:ip:192.168.1.2";

        // Register policy allows 3 attempts per 10 minutes
        for (int i = 0; i < 3; i++) {
            ConsumptionProbe probe = rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.REGISTER);
            assertTrue(probe.isConsumed(), "Register attempt " + (i + 1) + " should be allowed");
        }

        ConsumptionProbe exceededProbe = rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.REGISTER);
        assertFalse(exceededProbe.isConsumed(), "4th register attempt should be blocked");
    }

    @Test
    void tryConsume_differentKeys_doNotInterfere() {
        String key1 = "test:user:1";
        String key2 = "test:user:2";

        // Exhaust key1
        for (int i = 0; i < 5; i++) {
            rateLimiterService.tryConsume(key1, RateLimiterService.PolicyType.LOGIN);
        }
        assertFalse(rateLimiterService.tryConsume(key1, RateLimiterService.PolicyType.LOGIN).isConsumed());

        // Key2 should still have full capacity
        assertTrue(rateLimiterService.tryConsume(key2, RateLimiterService.PolicyType.LOGIN).isConsumed());
    }
}
