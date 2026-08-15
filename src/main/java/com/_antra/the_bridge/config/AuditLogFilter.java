package com._antra.the_bridge.config;

import com._antra.the_bridge.entity.AuditLog;
import com._antra.the_bridge.repository.AuditLogRepository;
import com._antra.the_bridge.repository.UserRepository;
import com._antra.the_bridge.security.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ClientIpResolver clientIpResolver;

    public AuditLogFilter(AuditLogRepository auditLogRepository, UserRepository userRepository, ClientIpResolver clientIpResolver) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            // Only log API calls, skip WebSocket, health and static resources
            String uri = request.getRequestURI();
            if (uri.startsWith("/api/") && !"/api/health".equals(uri)) {
                long duration = System.currentTimeMillis() - start;
                AuditLog log = new AuditLog();
                log.setAction(request.getMethod() + " " + uri + " [" + response.getStatus() + "] " + duration + "ms");
                log.setDescription(request.getQueryString() != null ? "Query: " + request.getQueryString() : null);
                log.setIpAddress(clientIpResolver.resolveClientIp(request));
                log.setCreatedAt(LocalDateTime.now());

                // Try to associate user
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    try {
                        userRepository.findByEmail(auth.getName()).ifPresent(log::setUser);
                    } catch (Exception ignored) {}
                }
                try {
                    auditLogRepository.save(log);
                } catch (Exception ignored) {}
            }
        }
    }
}
