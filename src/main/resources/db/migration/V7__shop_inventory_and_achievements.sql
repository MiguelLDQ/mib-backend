-- Fase 7: loja de personalizacao, inventario e conquistas

ALTER TABLE users ADD COLUMN xp_wallet BIGINT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN current_login_streak INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN longest_login_streak INTEGER NOT NULL DEFAULT 0;

-- Usuarios existentes ja tinham XP acumulado antes desta fase: credita o saldo
-- correspondente na carteira para que possam gastar o que ja conquistaram.
UPDATE users SET xp_wallet = total_xp;

CREATE TABLE shop_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(300),
    type VARCHAR(20) NOT NULL,
    price_xp INTEGER NOT NULL,
    icon_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    exclusive_to_achievement BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE user_inventory_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    shop_item_id UUID NOT NULL REFERENCES shop_items (id) ON DELETE CASCADE,
    source VARCHAR(20) NOT NULL,
    equipped BOOLEAN NOT NULL DEFAULT false,
    unlocked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_inventory_item UNIQUE (user_id, shop_item_id)
);

CREATE INDEX idx_user_inventory_equipped ON user_inventory_items (user_id, equipped);

CREATE TABLE achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(300) NOT NULL,
    xp_reward INTEGER NOT NULL,
    reward_shop_item_id UUID REFERENCES shop_items (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements (id) ON DELETE CASCADE,
    unlocked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user ON user_achievements (user_id);

-- Itens compraveis na loja
INSERT INTO shop_items (id, name, description, type, price_xp, icon_url, exclusive_to_achievement) VALUES
    (gen_random_uuid(), 'Moldura Dourada', 'Uma moldura elegante em tom dourado para o seu avatar', 'FRAME', 200, null, false),
    (gen_random_uuid(), 'Moldura Estelar', 'Moldura com brilho suave de estrelas', 'FRAME', 150, null, false),
    (gen_random_uuid(), 'Fundo Aurora', 'Fundo de perfil inspirado na aurora boreal', 'BACKGROUND', 150, null, false),
    (gen_random_uuid(), 'Fundo Floresta', 'Fundo de perfil com tons verdes relaxantes', 'BACKGROUND', 100, null, false),
    (gen_random_uuid(), 'Icone Coruja', 'Icone de perfil alternativo em formato de coruja', 'ICON', 80, null, false),
    (gen_random_uuid(), 'Titulo: Sonhador', 'Exibe "Sonhador" ao lado do seu nome', 'TITLE', 100, null, false),
    (gen_random_uuid(), 'Efeito Brilho', 'Efeito visual sutil de brilho ao redor do avatar', 'VISUAL_EFFECT', 250, null, false),
    (gen_random_uuid(), 'Tema Noturno Violeta', 'Tema de cores para o proprio perfil em tons de violeta', 'THEME', 300, null, false);

-- Itens exclusivos, concedidos apenas por conquista (nao aparecem na loja)
INSERT INTO shop_items (id, name, description, type, price_xp, icon_url, exclusive_to_achievement) VALUES
    (gen_random_uuid(), 'Emblema Pioneiro', 'Concedido a quem deu o primeiro passo no MIB', 'BADGE', 0, null, true),
    (gen_random_uuid(), 'Titulo: Consistente', 'Exibe "Consistente" apos uma semana de acesso seguido', 'TITLE', 0, null, true),
    (gen_random_uuid(), 'Titulo: Veterano', 'Exibe "Veterano" apos um mes de acesso seguido', 'TITLE', 0, null, true);

-- Conquistas (vinculadas aos itens exclusivos acima quando aplicavel)
INSERT INTO achievements (id, code, name, description, xp_reward, reward_shop_item_id) VALUES
    (gen_random_uuid(), 'FIRST_LOGIN', 'Primeiro Passo', 'Criou sua conta no MIB', 20,
        (SELECT id FROM shop_items WHERE name = 'Emblema Pioneiro')),
    (gen_random_uuid(), 'FIRST_FRIEND', 'Primeira Amizade', 'Fez seu primeiro amigo no MIB', 20, null),
    (gen_random_uuid(), 'FIRST_MISSION_COMPLETED', 'Primeira Missao', 'Concluiu sua primeira missao diaria', 20, null),
    (gen_random_uuid(), 'FIRST_BREATHING_EXERCISE', 'Primeiro Respiro', 'Concluiu seu primeiro exercicio de respiracao', 20, null),
    (gen_random_uuid(), 'STREAK_7_DAYS', 'Uma Semana Inteira', 'Acessou o MIB por 7 dias seguidos', 50,
        (SELECT id FROM shop_items WHERE name = 'Titulo: Consistente')),
    (gen_random_uuid(), 'STREAK_30_DAYS', 'Um Mes de Dedicacao', 'Acessou o MIB por 30 dias seguidos', 150,
        (SELECT id FROM shop_items WHERE name = 'Titulo: Veterano')),
    (gen_random_uuid(), 'MESSAGES_100', 'Comunicador', 'Enviou 100 mensagens no chat', 50, null),
    (gen_random_uuid(), 'LEVEL_5', 'Nivel 5', 'Alcancou o nivel 5', 30, null),
    (gen_random_uuid(), 'LEVEL_10', 'Nivel 10', 'Alcancou o nivel 10', 60, null),
    (gen_random_uuid(), 'LEVEL_20', 'Nivel 20', 'Alcancou o nivel 20', 120, null);
