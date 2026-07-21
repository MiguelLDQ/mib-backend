package com.mib.backend.moderation;

import com.mib.backend.config.ModerationProperties;
import com.mib.backend.entity.ModerationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Avalia um texto e retorna a violacao mais grave encontrada, se houver. A ordem de
 * verificacao prioriza categorias mais graves (odio, ameaca, assedio) antes de spam.
 * Comparacoes ignoram acentos e caixa para reduzir falsos negativos triviais.
 */
@Component
@RequiredArgsConstructor
public class ContentModerationEngine {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+|www\\.\\S+", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPEATED_CHAR_PATTERN = Pattern.compile("(.)\\1{6,}");

    private final ModerationProperties properties;

    public Optional<Violation> evaluate(String rawContent) {
        if (!properties.isEnabled() || rawContent == null || rawContent.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalize(rawContent);

        Optional<String> hate = firstMatch(normalized, properties.getHateSpeechTerms());
        if (hate.isPresent()) {
            return Optional.of(new Violation(ModerationCategory.HATE_SPEECH, hate.get()));
        }

        Optional<String> threat = firstMatch(normalized, properties.getThreatPhrases());
        if (threat.isPresent()) {
            return Optional.of(new Violation(ModerationCategory.THREATS_VIOLENCE, threat.get()));
        }

        Optional<String> harassment = firstMatch(normalized, properties.getHarassmentTerms());
        if (harassment.isPresent()) {
            return Optional.of(new Violation(ModerationCategory.HARASSMENT_BULLYING, harassment.get()));
        }

        if (isSpam(normalized, rawContent)) {
            return Optional.of(new Violation(ModerationCategory.SPAM, "padrao de spam"));
        }

        return Optional.empty();
    }

    private boolean isSpam(String normalized, String rawContent) {
        if (firstMatch(normalized, properties.getSpamPhrases()).isPresent()) {
            return true;
        }
        if (REPEATED_CHAR_PATTERN.matcher(rawContent).find()) {
            return true;
        }
        // Duas ou mais URLs na mesma mensagem e um forte indicativo de spam.
        long urlCount = URL_PATTERN.matcher(rawContent).results().count();
        return urlCount >= 2;
    }

    private Optional<String> firstMatch(String normalizedContent, java.util.List<String> terms) {
        return terms.stream()
                .filter(term -> !term.isBlank())
                .map(this::normalize)
                .filter(normalizedContent::contains)
                .findFirst();
    }

    private String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase();
    }

    public record Violation(ModerationCategory category, String matchedTerm) {
    }
}
