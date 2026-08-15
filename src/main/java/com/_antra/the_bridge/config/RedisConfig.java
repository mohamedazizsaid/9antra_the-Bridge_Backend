package com._antra.the_bridge.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Duration;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.url:${REDIS_URL:}}")
    private String redisUrl;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedisURI redisURI() {
        if (StringUtils.hasText(redisUrl)) {
            try {
                log.info("Configuring Redis using REDIS_URL environment variable.");
                return RedisURI.create(URI.create(redisUrl));
            } catch (Exception e) {
                log.error("Failed to parse REDIS_URL: {}, falling back to host/port configuration.", e.getMessage());
            }
        }

        RedisURI.Builder builder = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withTimeout(Duration.ofSeconds(2));

        if (StringUtils.hasText(redisPassword)) {
            builder.withPassword(redisPassword.toCharArray());
        }
        return builder.build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisURI redisURI) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisURI.getHost());
        config.setPort(redisURI.getPort());
        if (StringUtils.hasText(redisPassword)) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisClient lettuceRedisClient(RedisURI redisURI) {
        return RedisClient.create(redisURI);
    }

    @Bean
    public StatefulRedisConnection<String, byte[]> lettuceConnection(RedisClient redisClient) {
        try {
            RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
            return redisClient.connect(codec);
        } catch (Exception e) {
            log.warn("Could not establish immediate Lettuce connection to Redis: {}. Rate limiting will fallback gracefully.", e.getMessage());
            return null;
        }
    }

    @Bean
    public ProxyManager<String> proxyManager(StatefulRedisConnection<String, byte[]> lettuceConnection) {
        if (lettuceConnection == null) {
            log.warn("ProxyManager initialized without active Redis connection. Local fallback will be active.");
            return null;
        }
        try {
            return LettuceBasedProxyManager.builderFor(lettuceConnection)
                    .build();
        } catch (Exception e) {
            log.error("Error creating Bucket4j ProxyManager with Lettuce: {}", e.getMessage());
            return null;
        }
    }
}
