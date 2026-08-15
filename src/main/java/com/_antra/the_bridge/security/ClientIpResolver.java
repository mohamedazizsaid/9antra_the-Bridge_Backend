package com._antra.the_bridge.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientIpResolver {

    /**
     * Safely resolves the real client IP address behind reverse proxies (like Render).
     * Render and standard reverse proxies forward the original client IP in the X-Forwarded-For header.
     * When X-Forwarded-For contains a chain of IPs (e.g., "203.0.113.195, 70.41.3.18, 150.172.238.178"),
     * the leftmost non-private IP represents the client.
     */
    public String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            String[] ips = xForwardedFor.split(",");
            for (String ip : ips) {
                String cleanIp = ip.trim();
                if (isValidPublicIp(cleanIp)) {
                    return cleanIp;
                }
            }
            // Fallback to first IP in X-Forwarded-For if all match
            String firstIp = ips[0].trim();
            if (StringUtils.hasText(firstIp) && !"unknown".equalsIgnoreCase(firstIp)) {
                return firstIp;
            }
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "0.0.0.0";
    }

    private boolean isValidPublicIp(String ip) {
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        // Filter out localhost and standard private RFC 1918 addresses if part of forward chain
        return !ip.startsWith("127.") && !ip.startsWith("10.") && !ip.startsWith("192.168.")
                && !ip.startsWith("0:0:0:0:0:0:0:1") && !"::1".equals(ip);
    }
}
