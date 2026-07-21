package com.mib.backend.service.impl;

import com.mib.backend.ai.AiContextBuilder;
import com.mib.backend.ai.AiRateLimiter;
import com.mib.backend.ai.AiSystemPrompt;
import com.mib.backend.ai.GroqClient;
import com.mib.backend.dto.response.AiMessageResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.AiMessage;
import com.mib.backend.entity.AiMessageRole;
import com.mib.backend.entity.User;
import com.mib.backend.exception.AiRateLimitExceededException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.AiMessageRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AiConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private static final String WELCOME_MESSAGE = """
            Oi! Eu sou a assistente de apoio emocional do MIB. Estou aqui para te \
            ouvir, ajudar a organizar o que voce esta sentindo e sugerir praticas do \
            app quando fizer sentido. So um lembrete importante: eu nao substituo \
            acompanhamento profissional — se em algum momento voce precisar de mais \
            apoio, vale muito conversar com um psicologo, psiquiatra ou alguem de \
            confianca. Como voce esta hoje?""";

    private final AiMessageRepository aiMessageRepository;
    private final UserRepository userRepository;
    private final AiContextBuilder contextBuilder;
    private final GroqClient groqClient;
    private final AiRateLimiter rateLimiter;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AiMessageResponse> getHistory(UUID userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = aiMessageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PagedResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional
    public AiMessageResponse sendMessage(UUID userId, String content) {
        if (!rateLimiter.tryConsume(userId)) {
            throw new AiRateLimitExceededException(
                    "Voce atingiu o limite de mensagens para a IA nesta hora. Tente novamente daqui a pouco.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        boolean firstEverMessage = aiMessageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId).isEmpty();
        if (firstEverMessage) {
            aiMessageRepository.save(new AiMessage(user, AiMessageRole.ASSISTANT, WELCOME_MESSAGE));
        }

        aiMessageRepository.save(new AiMessage(user, AiMessageRole.USER, content.trim()));

        List<AiMessage> recentHistory = new ArrayList<>(aiMessageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId));
        Collections.reverse(recentHistory); // ordem cronologica para a chamada da API

        List<GroqClient.GroqChatMessage> apiMessages = new ArrayList<>();
        apiMessages.add(new GroqClient.GroqChatMessage("system", AiSystemPrompt.CONTENT));
        apiMessages.add(new GroqClient.GroqChatMessage("system", contextBuilder.build(user)));
        recentHistory.forEach(m -> apiMessages.add(
                new GroqClient.GroqChatMessage(m.getRole() == AiMessageRole.USER ? "user" : "assistant", m.getContent())));

        String reply = groqClient.chat(apiMessages);

        AiMessage saved = aiMessageRepository.save(new AiMessage(user, AiMessageRole.ASSISTANT, reply));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void clearHistory(UUID userId) {
        aiMessageRepository.deleteAllByUserId(userId);
    }

    private AiMessageResponse toResponse(AiMessage message) {
        return new AiMessageResponse(message.getId(), message.getRole().name(), message.getContent(), message.getCreatedAt());
    }
}
