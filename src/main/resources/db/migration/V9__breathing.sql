-- Fase 9: respiracao guiada

CREATE TABLE breathing_techniques (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(400) NOT NULL,
    benefits VARCHAR(400) NOT NULL,
    inhale_seconds INTEGER NOT NULL,
    hold_after_inhale_seconds INTEGER NOT NULL,
    exhale_seconds INTEGER NOT NULL,
    hold_after_exhale_seconds INTEGER NOT NULL,
    suggested_cycles INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE breathing_session_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    technique_id UUID NOT NULL REFERENCES breathing_techniques (id) ON DELETE CASCADE,
    duration_seconds INTEGER NOT NULL,
    xp_awarded BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_breathing_logs_user ON breathing_session_logs (user_id, created_at DESC);
CREATE INDEX idx_breathing_logs_user_xp ON breathing_session_logs (user_id, xp_awarded, created_at);

-- 7 tecnicas guiadas (Box Breathing e Respiracao Quadrada foram consolidadas em uma so,
-- ja que sao a mesma tecnica com nomes diferentes em ingles/portugues)
INSERT INTO breathing_techniques
    (id, code, name, description, benefits, inhale_seconds, hold_after_inhale_seconds, exhale_seconds, hold_after_exhale_seconds, suggested_cycles) VALUES
    (gen_random_uuid(), '4-7-8', 'Respiracao 4-7-8',
        'Inspire em 4 segundos, segure por 7 e expire lentamente em 8 segundos.',
        'Ajuda a reduzir a ansiedade e prepara o corpo para o sono.',
        4, 7, 8, 0, 4),
    (gen_random_uuid(), 'BOX_BREATHING', 'Respiracao Quadrada (Box Breathing)',
        'Inspire, segure, expire e segure novamente, todos os passos com a mesma duracao de 4 segundos.',
        'Melhora o foco e a estabilidade emocional, muito usada antes de momentos de tensao.',
        4, 4, 4, 4, 6),
    (gen_random_uuid(), 'DIAPHRAGMATIC', 'Respiracao Diafragmatica',
        'Inspire profundamente pelo abdomen, sentindo a barriga expandir, e expire devagar.',
        'Reduz a tensao muscular e melhora a oxigenacao do corpo.',
        5, 2, 6, 0, 6),
    (gen_random_uuid(), 'RELAXING', 'Respiracao Relaxante',
        'Ciclo suave e uniforme, sem pausas bruscas, focado em relaxar o corpo todo.',
        'Indicada para momentos de descompressao ao longo do dia.',
        4, 0, 6, 0, 6),
    (gen_random_uuid(), 'ANTI_ANXIETY', 'Respiracao Anti-Ansiedade',
        'Expiracao mais longa que a inspiracao, ativando a resposta natural de calma do corpo.',
        'Ajuda a diminuir sintomas fisicos de ansiedade em poucos minutos.',
        4, 2, 8, 0, 5),
    (gen_random_uuid(), 'SLEEP', 'Respiracao para Dormir',
        'Ciclo lento e prolongado, pensado para ser praticado deitado antes de dormir.',
        'Facilita o relaxamento do corpo e a transicao para o sono.',
        4, 4, 8, 2, 4),
    (gen_random_uuid(), 'ENERGIZING', 'Respiracao Energizante',
        'Ciclo mais curto e ritmado, com inspiracoes e expiracoes rapidas e controladas.',
        'Aumenta a sensacao de alerta e disposicao, indicada para o inicio do dia.',
        2, 0, 2, 0, 10);
