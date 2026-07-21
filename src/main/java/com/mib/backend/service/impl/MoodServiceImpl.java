package com.mib.backend.service.impl;

import com.mib.backend.dto.request.RecordMoodRequest;
import com.mib.backend.dto.response.MoodResponse;
import com.mib.backend.entity.Mood;
import com.mib.backend.entity.MoodLevel;
import com.mib.backend.entity.User;
import com.mib.backend.exception.BadRequestException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.MoodRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Diario de humor: puro autorrelato do usuario, usado apenas para gerar o grafico
 * pessoal de evolucao emocional dele mesmo. Deliberadamente NAO concede XP (para nao
 * criar incentivo a registrar um humor "melhor" do que o real) e NUNCA aciona
 * moderacao, restricao ou qualquer consequencia — apenas armazena o dado.
 */
@Service
@RequiredArgsConstructor
public class MoodServiceImpl implements MoodService {

    private final MoodRepository moodRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MoodResponse recordToday(UUID currentUserId, RecordMoodRequest request) {
        MoodLevel moodLevel = parseMoodLevel(request.moodLevel());
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Mood mood = moodRepository.findByUserIdAndMoodDate(currentUserId, today)
                .orElseGet(() -> {
                    User user = userRepository.findById(currentUserId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
                    return new Mood(user, moodLevel, null, today);
                });

        mood.setMoodLevel(moodLevel);
        mood.setNote(request.note());

        return toResponse(moodRepository.save(mood));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MoodResponse> getToday(UUID currentUserId) {
        return moodRepository.findByUserIdAndMoodDate(currentUserId, LocalDate.now(ZoneOffset.UTC))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodResponse> getHistory(UUID currentUserId, int days) {
        LocalDate end = LocalDate.now(ZoneOffset.UTC);
        LocalDate start = end.minusDays(Math.max(1, days) - 1L);

        return moodRepository.findByUserIdAndMoodDateBetweenOrderByMoodDateAsc(currentUserId, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    private MoodLevel parseMoodLevel(String raw) {
        try {
            return MoodLevel.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Nivel de humor invalido: " + raw + " (use VERY_GOOD, GOOD, NEUTRAL, SAD ou VERY_SAD)");
        }
    }

    private MoodResponse toResponse(Mood mood) {
        return new MoodResponse(mood.getId(), mood.getMoodLevel().name(), mood.getNote(), mood.getMoodDate(), mood.getCreatedAt());
    }
}
