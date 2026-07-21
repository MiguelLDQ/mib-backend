package com.mib.backend.ai;

import com.mib.backend.config.GroqProperties;
import com.mib.backend.exception.AiServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Cliente fino para a API de chat completions da Groq. A Groq expoe uma API
 * compativel com o formato da OpenAI, entao o payload segue esse mesmo shape
 * (messages com role/content, max_tokens, temperature).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroqClient {

    private final GroqProperties properties;
    private final RestClient restClient = RestClient.create();

    public String chat(List<GroqChatMessage> messages) {
        if (!properties.isConfigured()) {
            throw new AiServiceUnavailableException(
                    "O servico de IA nao esta configurado. Defina a variavel de ambiente GROQ_API_KEY.");
        }

        var request = new GroqChatRequest(properties.getModel(), messages,
                properties.getMaxTokens(), properties.getTemperature());

        try {
            GroqChatResponse response = restClient.post()
                    .uri(properties.getBaseUrl())
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(GroqChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AiServiceUnavailableException("A IA nao retornou uma resposta valida. Tente novamente.");
            }

            return response.choices().get(0).message().content();
        } catch (AiServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Falha ao chamar a API da Groq", ex);
            throw new AiServiceUnavailableException(
                    "Nao foi possivel falar com a IA agora. Tente novamente em instantes.");
        }
    }

    public record GroqChatMessage(String role, String content) {
    }

    private record GroqChatRequest(String model, List<GroqChatMessage> messages,
                                    int max_tokens, double temperature) {
    }

    private record GroqChatResponse(List<Choice> choices) {
        private record Choice(GroqChatMessage message) {
        }
    }
}
