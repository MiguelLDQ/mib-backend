package com.mib.backend.service.impl;

import com.mib.backend.config.ModerationProperties;
import com.mib.backend.entity.ContentTargetType;
import com.mib.backend.entity.ModerationActionType;
import com.mib.backend.entity.ModerationLog;
import com.mib.backend.entity.User;
import com.mib.backend.moderation.ContentModerationEngine;
import com.mib.backend.repository.ModerationLogRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Aplica a politica de escalada de consequencias descrita na especificacao do MIB:
 * infracao detectada -> conteudo removido + log registrado + warning contabilizado;
 * ao atingir os limiares configurados, o usuario e suspenso temporariamente e, em
 * reincidencia continua, banido automaticamente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationServiceImpl implements ModerationService {

    private final ContentModerationEngine engine;
    private final ModerationLogRepository moderationLogRepository;
    private final UserRepository userRepository;
    private final ModerationProperties properties;

    @Override
    @Transactional
    public boolean moderate(User author, ContentTargetType targetType, UUID targetId, String content) {
        var violation = engine.evaluate(content);
        if (violation.isEmpty()) {
            return false;
        }

        var category = violation.get().category();
        moderationLogRepository.save(new ModerationLog(
                author, targetType, targetId, category, ModerationActionType.AUTO_REMOVED,
                "Termo/padrao detectado: " + violation.get().matchedTerm()));

        applyEscalation(author, targetType, targetId, category);

        log.info("Conteudo removido automaticamente (usuario={}, categoria={}, alvo={}/{})",
                author.getId(), category, targetType, targetId);

        return true;
    }

    private void applyEscalation(User author, ContentTargetType targetType, UUID targetId,
                                  com.mib.backend.entity.ModerationCategory category) {
        author.setWarningCount(author.getWarningCount() + 1);
        int warnings = author.getWarningCount();

        ModerationActionType actionTaken = ModerationActionType.WARNING_ISSUED;
        String detail = "Advertencia " + warnings;

        if (warnings >= properties.getBanThreshold()) {
            author.setBanned(true);
            actionTaken = ModerationActionType.BANNED;
            detail = "Banimento automatico apos " + warnings + " advertencias";
        } else if (warnings >= properties.getSuspensionLongThreshold()) {
            author.setSuspendedUntil(Instant.now().plusSeconds(properties.getSuspensionLongHours() * 3600));
            actionTaken = ModerationActionType.SUSPENDED;
            detail = "Suspensao de " + properties.getSuspensionLongHours() + "h apos " + warnings + " advertencias";
        } else if (warnings >= properties.getSuspensionShortThreshold()) {
            author.setSuspendedUntil(Instant.now().plusSeconds(properties.getSuspensionShortHours() * 3600));
            actionTaken = ModerationActionType.SUSPENDED;
            detail = "Suspensao de " + properties.getSuspensionShortHours() + "h apos " + warnings + " advertencias";
        }

        userRepository.save(author);

        if (actionTaken != ModerationActionType.WARNING_ISSUED) {
            moderationLogRepository.save(new ModerationLog(author, targetType, targetId, category, actionTaken, detail));
        }
    }
}
