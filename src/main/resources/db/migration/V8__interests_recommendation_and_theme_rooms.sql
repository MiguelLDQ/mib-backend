-- Fase 8: interesses, recomendacao e salas tematicas

CREATE TABLE interests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(60) NOT NULL UNIQUE,
    icon_name VARCHAR(60),
    theme_room_id UUID REFERENCES chat_rooms (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE user_interests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    interest_id UUID NOT NULL REFERENCES interests (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_interest UNIQUE (user_id, interest_id)
);

CREATE INDEX idx_user_interests_interest ON user_interests (interest_id);

-- Salas tematicas (ChatRoom do tipo THEME) citadas na especificacao
INSERT INTO chat_rooms (id, type, name) VALUES
    (gen_random_uuid(), 'THEME', 'Sala Anime'),
    (gen_random_uuid(), 'THEME', 'Sala Programacao'),
    (gen_random_uuid(), 'THEME', 'Sala Musica'),
    (gen_random_uuid(), 'THEME', 'Sala Academia'),
    (gen_random_uuid(), 'THEME', 'Sala Livros'),
    (gen_random_uuid(), 'THEME', 'Sala Saude Mental'),
    (gen_random_uuid(), 'THEME', 'Sala Natureza');

-- Catalogo de interesses, associando os que tem sala tematica correspondente
INSERT INTO interests (id, name, theme_room_id) VALUES
    (gen_random_uuid(), 'Animes', (SELECT id FROM chat_rooms WHERE name = 'Sala Anime')),
    (gen_random_uuid(), 'Filmes', null),
    (gen_random_uuid(), 'Livros', (SELECT id FROM chat_rooms WHERE name = 'Sala Livros')),
    (gen_random_uuid(), 'Musica', (SELECT id FROM chat_rooms WHERE name = 'Sala Musica')),
    (gen_random_uuid(), 'Jogos', null),
    (gen_random_uuid(), 'Programacao', (SELECT id FROM chat_rooms WHERE name = 'Sala Programacao')),
    (gen_random_uuid(), 'Tecnologia', null),
    (gen_random_uuid(), 'Academia', (SELECT id FROM chat_rooms WHERE name = 'Sala Academia')),
    (gen_random_uuid(), 'Esportes', null),
    (gen_random_uuid(), 'Natureza', (SELECT id FROM chat_rooms WHERE name = 'Sala Natureza')),
    (gen_random_uuid(), 'Arte', null),
    (gen_random_uuid(), 'Fotografia', null),
    (gen_random_uuid(), 'Meditacao', null),
    (gen_random_uuid(), 'Psicologia', null),
    (gen_random_uuid(), 'Saude Mental', (SELECT id FROM chat_rooms WHERE name = 'Sala Saude Mental')),
    (gen_random_uuid(), 'Outros', null);
