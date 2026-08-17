-- ============================================================
-- Módulo de recomendação de conteúdo (vídeos/artigos) via IA
-- ============================================================

CREATE TABLE recommended_content (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(200) NOT NULL,
    description      VARCHAR(1000) NOT NULL,
    url              VARCHAR(500) NOT NULL,
    thumbnail_url    VARCHAR(500),
    content_type     VARCHAR(30) NOT NULL,
    category         VARCHAR(30) NOT NULL,
    duration_minutes INTEGER,
    source           VARCHAR(150),
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE recommended_content_tags (
    content_id UUID NOT NULL REFERENCES recommended_content(id) ON DELETE CASCADE,
    tag        VARCHAR(100) NOT NULL
);

CREATE TABLE user_content_recommendation (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_id       UUID NOT NULL REFERENCES recommended_content(id) ON DELETE CASCADE,
    relevance_score  DOUBLE PRECISION NOT NULL,
    ai_reason        VARCHAR(500),
    shown            BOOLEAN NOT NULL DEFAULT FALSE,
    clicked          BOOLEAN NOT NULL DEFAULT FALSE,
    recommended_for  DATE NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_recommended_content_category ON recommended_content(category) WHERE active = TRUE;
CREATE INDEX idx_user_content_rec_user_date ON user_content_recommendation(user_id, recommended_for);
CREATE UNIQUE INDEX idx_user_content_rec_unique ON user_content_recommendation(user_id, content_id, recommended_for);
