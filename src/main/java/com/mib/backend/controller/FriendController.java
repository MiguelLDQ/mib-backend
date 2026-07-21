package com.mib.backend.controller;

import com.mib.backend.dto.response.FriendRequestResponse;
import com.mib.backend.dto.response.FriendSummary;
import com.mib.backend.dto.response.UserSearchResult;
import com.mib.backend.security.SecurityUtils;
import com.mib.backend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Friends", description = "Busca de usuarios, solicitacoes e lista de amigos")
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/api/users/search")
    @Operation(summary = "Busca usuarios por username ou nome de exibicao")
    public ResponseEntity<List<UserSearchResult>> searchUsers(@RequestParam("query") String query) {
        return ResponseEntity.ok(friendService.searchUsers(SecurityUtils.currentUserId(), query));
    }

    @GetMapping("/api/friends")
    @Operation(summary = "Lista os amigos do usuario autenticado, com status online e ultimo acesso")
    public ResponseEntity<List<FriendSummary>> listFriends() {
        return ResponseEntity.ok(friendService.listFriends(SecurityUtils.currentUserId()));
    }

    @DeleteMapping("/api/friends/{friendUserId}")
    @Operation(summary = "Remove uma amizade existente")
    public ResponseEntity<Void> removeFriend(@PathVariable UUID friendUserId) {
        friendService.removeFriend(SecurityUtils.currentUserId(), friendUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/friends/requests/incoming")
    @Operation(summary = "Lista solicitacoes de amizade recebidas e pendentes")
    public ResponseEntity<List<FriendRequestResponse>> listIncomingRequests() {
        return ResponseEntity.ok(friendService.listIncomingRequests(SecurityUtils.currentUserId()));
    }

    @GetMapping("/api/friends/requests/outgoing")
    @Operation(summary = "Lista solicitacoes de amizade enviadas e pendentes")
    public ResponseEntity<List<FriendRequestResponse>> listOutgoingRequests() {
        return ResponseEntity.ok(friendService.listOutgoingRequests(SecurityUtils.currentUserId()));
    }

    @PostMapping("/api/friends/requests/{targetUserId}")
    @Operation(summary = "Envia uma solicitacao de amizade")
    public ResponseEntity<Void> sendRequest(@PathVariable UUID targetUserId) {
        friendService.sendRequest(SecurityUtils.currentUserId(), targetUserId);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/api/friends/requests/{requestId}/accept")
    @Operation(summary = "Aceita uma solicitacao de amizade recebida")
    public ResponseEntity<Void> acceptRequest(@PathVariable UUID requestId) {
        friendService.acceptRequest(SecurityUtils.currentUserId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/friends/requests/{requestId}/decline")
    @Operation(summary = "Recusa uma solicitacao de amizade recebida")
    public ResponseEntity<Void> declineRequest(@PathVariable UUID requestId) {
        friendService.declineRequest(SecurityUtils.currentUserId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/friends/requests/{requestId}")
    @Operation(summary = "Cancela uma solicitacao de amizade enviada")
    public ResponseEntity<Void> cancelRequest(@PathVariable UUID requestId) {
        friendService.cancelRequest(SecurityUtils.currentUserId(), requestId);
        return ResponseEntity.noContent().build();
    }
}
