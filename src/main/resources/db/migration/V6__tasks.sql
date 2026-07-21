-- Fase 6: tarefas pessoais do usuario

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    due_date DATE,
    completed_at TIMESTAMP WITH TIME ZONE,
    xp_awarded BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_user_status ON tasks (user_id, status);
CREATE INDEX idx_tasks_user_category ON tasks (user_id, category);
