-- Fase 3: XP, niveis e missoes diarias

ALTER TABLE users ADD COLUMN total_xp BIGINT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN level INTEGER NOT NULL DEFAULT 1;

CREATE TABLE xp_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    amount INTEGER NOT NULL,
    reason VARCHAR(40) NOT NULL,
    description VARCHAR(200),
    level_after INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_xp_history_user_id ON xp_history (user_id, created_at DESC);

CREATE TABLE mission_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(120) NOT NULL,
    description VARCHAR(300) NOT NULL,
    category VARCHAR(30) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    base_xp_reward INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE daily_missions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_template_id UUID NOT NULL REFERENCES mission_templates (id) ON DELETE CASCADE,
    mission_date DATE NOT NULL,
    xp_reward INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_daily_missions_date ON daily_missions (mission_date);

CREATE TABLE user_mission_completions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    daily_mission_id UUID NOT NULL REFERENCES daily_missions (id) ON DELETE CASCADE,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_daily_mission UNIQUE (user_id, daily_mission_id)
);

-- Seed inicial do banco de missoes (o scheduler sorteia 5 por dia entre os ativos)
INSERT INTO mission_templates (id, title, description, category, difficulty, base_xp_reward) VALUES
    (gen_random_uuid(), 'Beba agua', 'Beba pelo menos 2 litros de agua ao longo do dia', 'HYDRATION', 'EASY', 15),
    (gen_random_uuid(), 'Caminhada leve', 'Faca uma caminhada de 15 minutos ao ar livre', 'EXERCISE', 'EASY', 20),
    (gen_random_uuid(), 'Respiracao guiada', 'Pratique um exercicio de respiracao completo', 'BREATHING', 'EASY', 15),
    (gen_random_uuid(), 'Durma cedo', 'Va dormir antes das 23h hoje', 'SLEEP', 'MEDIUM', 25),
    (gen_random_uuid(), 'Escreva algo positivo', 'Escreva uma frase positiva sobre o seu dia', 'POSITIVITY', 'EASY', 15),
    (gen_random_uuid(), 'Converse com um amigo', 'Envie uma mensagem gentil para um amigo', 'SOCIAL', 'EASY', 20),
    (gen_random_uuid(), 'Musica relaxante', 'Ouca uma musica relaxante por 10 minutos', 'MINDFULNESS', 'EASY', 10),
    (gen_random_uuid(), 'Reduza as redes sociais', 'Passe pelo menos 2 horas longe das redes sociais', 'DIGITAL_WELLBEING', 'MEDIUM', 25),
    (gen_random_uuid(), 'Alongamento matinal', 'Faca 10 minutos de alongamento ao acordar', 'EXERCISE', 'EASY', 15),
    (gen_random_uuid(), 'Diario de gratidao', 'Escreva tres coisas pelas quais voce e grato', 'POSITIVITY', 'MEDIUM', 20),
    (gen_random_uuid(), 'Pausa consciente', 'Faca uma pausa de 5 minutos sem telas para respirar', 'MINDFULNESS', 'EASY', 10),
    (gen_random_uuid(), 'Organize seu dia', 'Planeje as tres prioridades do seu dia pela manha', 'MINDFULNESS', 'MEDIUM', 20);
