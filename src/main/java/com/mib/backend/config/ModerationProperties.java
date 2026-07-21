package com.mib.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Listas de termos/padroes usadas pelo motor de moderacao automatica. Os valores
 * padrao aqui sao apenas um ponto de partida ilustrativo — para producao, recomenda-se
 * expandir cada lista (via application.yml ou variavel de ambiente) com um vocabulario
 * mais completo, adequado ao publico do MIB.
 * <p>
 * Termos relacionados a sofrimento emocional, tristeza ou autolesao NAO devem ser
 * adicionados a nenhuma destas listas: o objetivo da moderacao e coibir abuso contra
 * outros usuarios (assedio, ameacas, discurso de odio, spam), nunca penalizar quem
 * esta relatando sofrimento proprio.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mib.moderation")
public class ModerationProperties {

    private boolean enabled = true;

    private List<String> harassmentTerms = new ArrayList<>(List.of(
            "idiota", "burro", "inutil", "imbecil", "verme"
    ));

    private List<String> threatPhrases = new ArrayList<>(List.of(
            "vou te matar", "vou te machucar", "vou te bater", "vou te achar e"
    ));

    private List<String> hateSpeechTerms = new ArrayList<>();

    private List<String> spamPhrases = new ArrayList<>(List.of(
            "compre agora", "ganhe dinheiro facil", "clique aqui", "renda extra garantida",
            "promocao imperdivel", "fature alto"
    ));

    /** Limite de warnings para suspensao curta (24h). */
    private int suspensionShortThreshold = 3;

    /** Duracao da suspensao curta, em horas. */
    private long suspensionShortHours = 24;

    /** Limite de warnings para suspensao longa (7 dias). */
    private int suspensionLongThreshold = 5;

    /** Duracao da suspensao longa, em horas. */
    private long suspensionLongHours = 168;

    /** Limite de warnings para banimento definitivo. */
    private int banThreshold = 7;
}
