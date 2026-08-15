package com._antra.the_bridge.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientIpResolverTest {

    private ClientIpResolver clientIpResolver;

    @BeforeEach
    void setUp() {
        clientIpResolver = new ClientIpResolver();
    }

    @Test
    void resolveClientIp_withSingleXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("203.0.113.195", ip);
    }

    @Test
    void resolveClientIp_withMultipleXForwardedFor_returnsFirstPublicIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "198.51.100.10, 203.0.113.195, 10.0.0.1");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("198.51.100.10", ip);
    }

    @Test
    void resolveClientIp_withXRealIp_fallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.50");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("198.51.100.50", ip);
    }

    @Test
    void resolveClientIp_withNoHeaders_returnsRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("192.168.1.100", ip);
    }
}
