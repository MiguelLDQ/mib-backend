package com.mib.backend.service.impl;

import com.mib.backend.entity.User;
import com.mib.backend.entity.XpHistory;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.event.LevelUpEvent;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.repository.XpHistoryRepository;
import com.mib.backend.service.XpService;
import com.mib.backend.util.LevelCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class XpServiceImpl implements XpService {

    private final UserRepository userRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public XpAwardResult awardXp(User user, int amount, XpReasonType reason, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("A quantidade de XP concedida deve ser positiva");
        }

        int previousLevel = user.getLevel();

        user.setTotalXp(user.getTotalXp() + amount);
        user.setXpWallet(user.getXpWallet() + amount);
        LevelCalculator.LevelProgress progress = LevelCalculator.calculate(user.getTotalXp());
        user.setLevel(progress.level());
        userRepository.save(user);

        xpHistoryRepository.save(new XpHistory(user, amount, reason, description, progress.level()));

        boolean leveledUp = progress.level() > previousLevel;
        if (leveledUp) {
            log.info("Usuario {} subiu para o nivel {}", user.getId(), progress.level());
            eventPublisher.publishEvent(new LevelUpEvent(user.getId(), progress.level()));
        }

        return new XpAwardResult(progress, leveledUp, previousLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelCalculator.LevelProgress getProgress(User user) {
        return LevelCalculator.calculate(user.getTotalXp());
    }
}
