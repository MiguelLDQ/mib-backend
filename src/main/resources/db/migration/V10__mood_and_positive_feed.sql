-- Fase 10: diario de humor e feed positivo

CREATE TABLE moods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    mood_level VARCHAR(20) NOT NULL,
    note VARCHAR(300),
    mood_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_mood_user_date UNIQUE (user_id, mood_date)
);

CREATE INDEX idx_moods_user_date ON moods (user_id, mood_date DESC);

CREATE TABLE positive_feed_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL,
    content VARCHAR(400) NOT NULL,
    author VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

INSERT INTO positive_feed_items (id, type, content, author) VALUES
    (gen_random_uuid(), 'QUOTE', 'Voce nao precisa ter tudo resolvido hoje. Um passo de cada vez ja e progresso.', null),
    (gen_random_uuid(), 'QUOTE', 'Cuidar da propria mente e um ato de coragem, nao de fraqueza.', null),
    (gen_random_uuid(), 'QUOTE', 'Dias dificeis tambem passam. Voce ja superou 100% dos seus piores dias ate aqui.', null),
    (gen_random_uuid(), 'FACT', 'Respirar profundamente por apenas 1 minuto pode reduzir a frequencia cardiaca e a sensacao de ansiedade.', null),
    (gen_random_uuid(), 'FACT', 'Dormir bem regula o humor tanto quanto a alimentacao e o exercicio fisico.', null),
    (gen_random_uuid(), 'FACT', 'Conversar sobre o que sentimos com alguem de confianca reduz a intensidade emocional do problema.', null),
    (gen_random_uuid(), 'CHALLENGE', 'Desafio rapido: beba um copo de agua agora e perceba como o corpo reage.', null),
    (gen_random_uuid(), 'CHALLENGE', 'Desafio rapido: mande uma mensagem gentil para alguem que voce nao fala ha um tempo.', null),
    (gen_random_uuid(), 'CHALLENGE', 'Desafio rapido: liste 3 coisas, por menores que sejam, pelas quais voce e grato hoje.', null),
    (gen_random_uuid(), 'WELLNESS_TIP', 'Se sentir a mente acelerada, tente focar em 5 coisas que voce pode ver ao seu redor agora.', null),
    (gen_random_uuid(), 'WELLNESS_TIP', 'Pausas curtas e frequentes ao longo do dia ajudam mais a concentracao do que uma unica pausa longa.', null),
    (gen_random_uuid(), 'WELLNESS_TIP', 'Reduzir o tempo de tela antes de dormir melhora significativamente a qualidade do sono.', null);
