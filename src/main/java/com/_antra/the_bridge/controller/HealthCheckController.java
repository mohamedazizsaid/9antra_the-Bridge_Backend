package com._antra.the_bridge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Endpoint de contrôle de l'état de l'application (DB, Redis)")
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    private final DataSource dataSource;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    public HealthCheckController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    @Operation(summary = "Vérifier la santé de l'API", description = "Teste les connexions à la base de données MySQL et à Redis.")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> statusMap = new HashMap<>();
        boolean dbHealthy = checkDatabaseHealth();
        boolean redisHealthy = checkRedisHealth();

        statusMap.put("status", (dbHealthy && redisHealthy) ? "UP" : "DEGRADED");
        statusMap.put("database", dbHealthy ? "UP" : "DOWN");
        statusMap.put("redis", redisHealthy ? "UP" : "DOWN");

        HttpStatus httpStatus = dbHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return new ResponseEntity<>(statusMap, httpStatus);
    }

    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkRedisHealth() {
        if (redisConnectionFactory == null) {
            return false;
        }
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            return "PONG".equalsIgnoreCase(connection.ping());
        } catch (Exception e) {
            log.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
}
