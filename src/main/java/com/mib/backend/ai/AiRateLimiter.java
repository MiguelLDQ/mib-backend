package com.mib.backend.ai;

import com.mib.backend.config.GroqProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AiRateLimiter {

    private final GroqProperties properties;
    private final Map<UUID, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(UUID userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, id -> Bucket.builder()
                .addLimit(Bandwidth.classic(properties.getRateLimitPerHour(),
                        Refill.greedy(properties.getRateLimitPerHour(), Duration.ofHours(1))))
                .build());

        return bucket.tryConsume(1);
    }
}
