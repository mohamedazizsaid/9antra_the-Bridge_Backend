package com._antra.the_bridge;

import com._antra.the_bridge.security.ClientIpResolver;
import com._antra.the_bridge.security.RateLimiterService;
import com._antra.the_bridge.security.RateLimitingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RateLimitingIntegrationTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/api/auth")
    static class DummyAuthController {
        @PostMapping("/login")
        public ResponseEntity<String> login() {
            return ResponseEntity.ok("OK");
        }
    }

    @BeforeEach
    void setUp() {
        ClientIpResolver ipResolver = new ClientIpResolver();
        RateLimiterService rateLimiterService = new RateLimiterService();
        ObjectMapper objectMapper = new ObjectMapper();

        RateLimitingFilter rateLimitingFilter = new RateLimitingFilter(ipResolver, rateLimiterService, objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(new DummyAuthController())
                .addFilters(rateLimitingFilter)
                .build();
    }

    @Test
    void loginRateLimiting_returns429WhenExceeded() throws Exception {
        String testIp = "198.51.100.99";

        // Perform 5 allowed login attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", testIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        // 6th attempt should be blocked with 429 Too Many Requests
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", testIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }

    @Test
    void differentIp_isNotBlockedByExhaustedIp() throws Exception {
        String exhaustedIp = "198.51.100.88";
        String freshIp = "198.51.100.89";

        // Exhaust exhaustedIp
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .header("X-Forwarded-For", exhaustedIp)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"));
        }

        // freshIp should still be allowed
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", freshIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}
