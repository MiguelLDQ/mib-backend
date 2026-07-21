-- Fase 4: chat geral e privado (WebSocket)

CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE chat_room_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id UUID NOT NULL REFERENCES chat_rooms (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_chat_room_participant UNIQUE (chat_room_id, user_id)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id UUID NOT NULL REFERENCES chat_rooms (id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content VARCHAR(1000) NOT NULL,
    removed_by_moderation BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_room_created_at ON messages (chat_room_id, created_at DESC);

-- Cria a sala geral unica usada por todos os usuarios.
INSERT INTO chat_rooms (id, type, name) VALUES (gen_random_uuid(), 'GENERAL', 'Sala Geral');
