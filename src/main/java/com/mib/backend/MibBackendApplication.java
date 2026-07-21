package com.mib.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MIB - Plataforma de saude mental, bem-estar, comunidade e gamificacao.
 * <p>
 * Fase 1 desta base: fundacao do projeto (config, entidades base e modulo de Auth).
 * Os demais modulos (profile, friends, chat, anonymous, tasks, shop, xp, breathing,
 * ai, admin, notifications, recommendation) serao adicionados nas fases seguintes,
 * seguindo a mesma estrutura em camadas ja estabelecida aqui.
 */
@SpringBootApplication
@EnableScheduling
public class MibBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MibBackendApplication.class, args);
    }
}
