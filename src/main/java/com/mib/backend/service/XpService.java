package com.mib.backend.service;

import com.mib.backend.entity.User;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.util.LevelCalculator;

public interface XpService {

    /**
     * Concede XP a um usuario, atualiza seu nivel e registra o historico.
     * Deve ser chamado por qualquer modulo que recompense uma acao com XP
     * (missoes, tarefas, respiracao, chat, conquistas etc.).
     *
     * @return o progresso de nivel apos a concessao, incluindo se houve level up.
     */
    XpAwardResult awardXp(User user, int amount, XpReasonType reason, String description);

    LevelCalculator.LevelProgress getProgress(User user);

    record XpAwardResult(LevelCalculator.LevelProgress progress, boolean leveledUp, int previousLevel) {
    }
}
