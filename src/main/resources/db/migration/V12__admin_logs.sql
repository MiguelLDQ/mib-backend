-- Fase 12: painel administrativo (trilha de auditoria)

CREATE TABLE admin_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    action VARCHAR(40) NOT NULL,
    target_type VARCHAR(30),
    target_id UUID,
    detail VARCHAR(300),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_admin_logs_created ON admin_logs (created_at DESC);
CREATE INDEX idx_admin_logs_admin ON admin_logs (admin_id, created_at DESC);
