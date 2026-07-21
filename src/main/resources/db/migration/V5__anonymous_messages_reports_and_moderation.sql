-- Fase 5: Estrelas Pontilhadas (mensagens anonimas), denuncias e log de moderacao

CREATE TABLE anonymous_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    parent_message_id UUID REFERENCES anonymous_messages (id) ON DELETE CASCADE,
    content VARCHAR(500) NOT NULL,
    like_count INTEGER NOT NULL DEFAULT 0,
    reply_count INTEGER NOT NULL DEFAULT 0,
    removed_by_moderation BOOLEAN NOT NULL DEFAULT false,
    removed_by_author BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_anonymous_messages_feed
    ON anonymous_messages (created_at DESC)
    WHERE parent_message_id IS NULL;

CREATE INDEX idx_anonymous_messages_parent ON anonymous_messages (parent_message_id);

CREATE TABLE anonymous_message_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES anonymous_messages (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_anonymous_message_like UNIQUE (message_id, user_id)
);

CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    reason VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_report_reporter_target UNIQUE (reporter_id, target_type, target_id)
);

CREATE INDEX idx_reports_status ON reports (status, created_at DESC);

CREATE TABLE moderation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    category VARCHAR(30) NOT NULL,
    action VARCHAR(30) NOT NULL,
    detail VARCHAR(300),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_moderation_logs_user ON moderation_logs (user_id, created_at DESC);
