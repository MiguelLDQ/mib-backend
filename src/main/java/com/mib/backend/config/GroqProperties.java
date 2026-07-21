package com.mib.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao do provedor de IA usado no modulo de apoio emocional. Groq oferece um
 * nivel gratuito com modelos open-weight (ex.: Llama 3.1), compativel com a API de
 * chat completions da OpenAI — a mesma abordagem ja usada no BMO.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mib.ai.groq")
public class GroqProperties {

    private String apiKey = "";

    private String baseUrl = "https://api.groq.com/openai/v1/chat/completions";

    private String model = "llama-3.1-8b-instant";

    private int maxTokens = 500;

    private double temperature = 0.7;

    /** Quantas mensagens recentes da conversa (usuario + IA) sao reenviadas como
     * contexto a cada chamada, para manter continuidade sem estourar o limite gratuito. */
    private int historyWindowSize = 10;

    /** Limite de mensagens que um usuario pode enviar a IA por hora (protecao de custo/abuso). */
    private int rateLimitPerHour = 20;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
