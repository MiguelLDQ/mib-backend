package com.mib.backend.service;

import com.mib.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mantem em memoria o ultimo instante de atividade de cada usuario autenticado
 * (atualizado a cada requisicao valida no {@code JwtAuthenticationFilter}), evitando
 * escrita no banco a cada requisicao. Um scheduler persiste periodicamente esses
 * valores em {@code users.last_active_at}, que tambem serve como "ultimo acesso".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private static final long ONLINE_THRESHOLD_SECONDS = 5 * 60;

    private final UserRepository userRepository;
    private final Map<UUID, Instant> lastSeenInMemory = new ConcurrentHashMap<>();

    public void touch(UUID userId) {
        lastSeenInMemory.put(userId, Instant.now());
    }

    public boolean isOnline(UUID userId, Instant persistedLastActiveAt) {
        Instant reference = lastSeenInMemory.getOrDefault(userId, persistedLastActiveAt);
        if (reference == null) {
            return false;
        }
        return reference.isAfter(Instant.now().minusSeconds(ONLINE_THRESHOLD_SECONDS));
    }

    public Instant lastSeen(UUID userId, Instant persistedLastActiveAt) {
        Instant inMemory = lastSeenInMemory.get(userId);
        if (inMemory == null) {
            return persistedLastActiveAt;
        }
        if (persistedLastActiveAt == null || inMemory.isAfter(persistedLastActiveAt)) {
            return inMemory;
        }
        return persistedLastActiveAt;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void flushToDatabase() {
        if (lastSeenInMemory.isEmpty()) {
            return;
        }
        Instant cutoff = Instant.now().minus(2, ChronoUnit.HOURS);
        lastSeenInMemory.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));

        lastSeenInMemory.forEach((userId, instant) ->
                userRepository.findById(userId).ifPresent(user -> {
                    user.setLastActiveAt(instant);
                    userRepository.save(user);
                }));

        log.debug("Presenca sincronizada para {} usuarios", lastSeenInMemory.size());
    }
}
