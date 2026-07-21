package com.mib.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Interesse do catalogo (ex.: "Animes", "Programacao"). Quando associado a uma
 * {@code themeRoom}, selecionar esse interesse adiciona o usuario automaticamente a
 * sala tematica correspondente. A "ThemeRoom" da especificacao e realizada aqui como
 * um {@link ChatRoom} do tipo {@link ChatRoomType#THEME} (reaproveitando toda a
 * infraestrutura de chat da Fase 4), em vez de uma entidade paralela redundante.
 */
@Entity
@Table(name = "interests")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Interest extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(name = "icon_name", length = 60)
    private String iconName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_room_id")
    private ChatRoom themeRoom;

    public Interest(String name, String iconName) {
        this.name = name;
        this.iconName = iconName;
    }
}
