-- Fase 2: perfil (presenca) e amizades

ALTER TABLE users ADD COLUMN last_active_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    addressee_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    responded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT chk_friendship_not_self CHECK (requester_id <> addressee_id)
);

-- Evita duplicar relacoes pendentes/aceitas no mesmo sentido.
CREATE UNIQUE INDEX uq_friendship_requester_addressee ON friendships (requester_id, addressee_id);

CREATE INDEX idx_friendships_addressee_status ON friendships (addressee_id, status);
CREATE INDEX idx_friendships_requester_status ON friendships (requester_id, status);
