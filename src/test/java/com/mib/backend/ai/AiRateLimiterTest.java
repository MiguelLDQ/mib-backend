package com.mib.backend.ai;

import com.mib.backend.config.GroqProperties;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AiRateLimiterTest {

    @Test
    void devePermitirAteOLimiteEBloquearDepois() {
        GroqProperties properties = new GroqProperties();
        properties.setRateLimitPerHour(3);

        AiRateLimiter limiter = new AiRateLimiter(properties);
        UUID userId = UUID.randomUUID();

        assertThat(limiter.tryConsume(userId)).isTrue();
        assertThat(limiter.tryConsume(userId)).isTrue();
        assertThat(limiter.tryConsume(userId)).isTrue();
        assertThat(limiter.tryConsume(userId)).isFalse();
    }

    @Test
    void deveControlarLimitesSeparadamentePorUsuario() {
        GroqProperties properties = new GroqProperties();
        properties.setRateLimitPerHour(1);

        AiRateLimiter limiter = new AiRateLimiter(properties);
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        assertThat(limiter.tryConsume(userA)).isTrue();
        assertThat(limiter.tryConsume(userA)).isFalse();
        assertThat(limiter.tryConsume(userB)).isTrue();
    }
}
