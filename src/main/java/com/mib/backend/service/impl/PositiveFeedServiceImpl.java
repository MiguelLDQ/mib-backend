package com.mib.backend.service.impl;

import com.mib.backend.dto.response.PositiveFeedItemResponse;
import com.mib.backend.entity.PositiveFeedItem;
import com.mib.backend.repository.PositiveFeedItemRepository;
import com.mib.backend.service.PositiveFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PositiveFeedServiceImpl implements PositiveFeedService {

    private final PositiveFeedItemRepository positiveFeedItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PositiveFeedItemResponse> getTodayFeed(int count) {
        List<PositiveFeedItem> active = positiveFeedItemRepository.findAllByActiveTrue();
        if (active.isEmpty()) {
            return List.of();
        }

        // Semente baseada no dia do ano garante o mesmo embaralhamento para todos os
        // usuarios durante o dia inteiro, e um novo automaticamente a cada virada de dia.
        long seed = LocalDate.now().toEpochDay();
        List<PositiveFeedItem> shuffled = new ArrayList<>(active);
        Collections.shuffle(shuffled, new Random(seed));

        return shuffled.stream()
                .limit(Math.max(1, count))
                .map(item -> new PositiveFeedItemResponse(item.getId(), item.getType().name(), item.getContent(), item.getAuthor()))
                .toList();
    }
}
