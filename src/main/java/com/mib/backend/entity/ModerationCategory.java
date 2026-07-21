package com.mib.backend.entity;

/**
 * Categorias de violacao que a moderacao automatica trata como infracao acionavel
 * (remove o conteudo, registra log e conta no historico do usuario).
 * <p>
 * IMPORTANTE: este filtro NUNCA deve mirar expressoes de sofrimento emocional,
 * tristeza ou ideacao de autolesao/suicidio do proprio usuario. Em um app de saude
 * mental, punir quem esta pedindo ajuda seria o oposto do proposito do produto — esse
 * tipo de conteudo deve ser tratado pelo modulo de IA de apoio emocional (fase futura),
 * nunca por moderacao automatica de infracao.
 */
public enum ModerationCategory {
    SPAM,
    HARASSMENT_BULLYING,
    THREATS_VIOLENCE,
    HATE_SPEECH
}
