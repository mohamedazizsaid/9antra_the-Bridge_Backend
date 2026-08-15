package com._antra.the_bridge.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ClientIpResolver clientIpResolver;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(ClientIpResolver clientIpResolver, RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.clientIpResolver = clientIpResolver;
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip non-API routes, Swagger, and health check endpoints
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = clientIpResolver.resolveClientIp(request);

        // 1. Global Rate Limit Check
        String globalKey = "rate:global:ip:" + clientIp;
        ConsumptionProbe globalProbe = rateLimiterService.tryConsume(globalKey, RateLimiterService.PolicyType.GLOBAL);
        if (!globalProbe.isConsumed()) {
            handleRateLimitExceeded(response, globalProbe, path, "Limite globale dépassée. Trop de requêtes depuis votre adresse IP.");
            return;
        }

        // 2. Specific Route Rate Limit Check
        ConsumptionProbe endpointProbe = checkEndpointRateLimit(request, clientIp, path);
        if (endpointProbe != null && !endpointProbe.isConsumed()) {
            handleRateLimitExceeded(response, endpointProbe, path, "Limite spécifique d'endpoint dépassée. Veuillez ralentir vos requêtes.");
            return;
        }

        // Add remaining limit header for global rate limit
        response.setHeader("X-RateLimit-Remaining", String.valueOf(globalProbe.getRemainingTokens()));

        filterChain.doFilter(request, response);
    }

    private ConsumptionProbe checkEndpointRateLimit(HttpServletRequest request, String clientIp, String path) {
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/oauth/login")) {
            String key = "rate:login:ip:" + clientIp;
            return rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.LOGIN);
        } else if (path.startsWith("/api/auth/register")) {
            String key = "rate:register:ip:" + clientIp;
            return rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.REGISTER);
        } else if (path.startsWith("/api/auth/forgot-password") || path.startsWith("/api/auth/reset-password")) {
            String key = "rate:reset:ip:" + clientIp;
            return rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.PASSWORD_RESET);
        } else if (path.startsWith("/api/certificates") || "POST".equalsIgnoreCase(request.getMethod()) && path.contains("/upload")) {
            String key = "rate:expensive:ip:" + clientIp;
            return rateLimiterService.tryConsume(key, RateLimiterService.PolicyType.EXPENSIVE);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String userKey = "rate:auth:user:" + auth.getName();
            return rateLimiterService.tryConsume(userKey, RateLimiterService.PolicyType.AUTHENTICATED);
        }

        return null;
    }

    private void handleRateLimitExceeded(HttpServletResponse response, ConsumptionProbe probe, String path, String message) throws IOException {
        long waitForRefillSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;

        log.warn("Rate limit exceeded on path {} for client. Seconds to wait: {}", path, waitForRefillSeconds);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(waitForRefillSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(waitForRefillSeconds));

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorDetails.put("error", HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
        errorDetails.put("message", message + " Veuillez réessayer dans " + waitForRefillSeconds + " secondes.");
        errorDetails.put("path", path);

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.startsWith("/ws") ||
               path.equals("/api/health") ||
               !path.startsWith("/api/");
    }
}
