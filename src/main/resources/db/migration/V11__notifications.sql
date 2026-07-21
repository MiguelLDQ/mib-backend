-- Fase 11: notificacoes

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(300) NOT NULL,
    related_type VARCHAR(30),
    related_id UUID,
    read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, read);
