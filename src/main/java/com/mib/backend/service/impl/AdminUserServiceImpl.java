package com.mib.backend.service.impl;

import com.mib.backend.dto.response.AdminUserResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.AdminActionType;
import com.mib.backend.entity.NotificationType;
import com.mib.backend.entity.Role;
import com.mib.backend.entity.RoleName;
import com.mib.backend.entity.User;
import com.mib.backend.exception.BadRequestException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.RoleRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AdminAuditService;
import com.mib.backend.service.AdminUserService;
import com.mib.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminAuditService adminAuditService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> search(String query, int page, int size) {
        var result = userRepository.searchForAdmin(query, PageRequest.of(page, size));
        return PagedResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getById(UUID userId) {
        return toResponse(findUserOrThrow(userId));
    }

    @Override
    @Transactional
    public AdminUserResponse suspend(UUID adminId, UUID targetUserId, int hours, String reason) {
        requireNotSelf(adminId, targetUserId, "Voce nao pode suspender a propria conta");

        User user = findUserOrThrow(targetUserId);
        user.setSuspendedUntil(Instant.now().plusSeconds(hours * 3600L));
        userRepository.save(user);

        String detail = "Suspenso por " + hours + "h" + (reason != null && !reason.isBlank() ? ": " + reason : "");
        auditAndNotify(adminId, targetUserId, user, AdminActionType.USER_SUSPENDED, detail,
                "Sua conta foi suspensa", detail);

        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse unsuspend(UUID adminId, UUID targetUserId) {
        User user = findUserOrThrow(targetUserId);
        user.setSuspendedUntil(null);
        userRepository.save(user);

        auditAndNotify(adminId, targetUserId, user, AdminActionType.USER_UNSUSPENDED,
                "Suspensao removida", "Sua conta voltou ao normal",
                "A suspensao da sua conta foi removida");

        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse ban(UUID adminId, UUID targetUserId, String reason) {
        requireNotSelf(adminId, targetUserId, "Voce nao pode banir a propria conta");

        User user = findUserOrThrow(targetUserId);
        user.setBanned(true);
        userRepository.save(user);

        String detail = "Banido" + (reason != null && !reason.isBlank() ? ": " + reason : "");
        auditAndNotify(adminId, targetUserId, user, AdminActionType.USER_BANNED, detail,
                "Sua conta foi banida", detail);

        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse unban(UUID adminId, UUID targetUserId) {
        User user = findUserOrThrow(targetUserId);
        user.setBanned(false);
        userRepository.save(user);

        auditAndNotify(adminId, targetUserId, user, AdminActionType.USER_UNBANNED,
                "Banimento removido", "Sua conta foi reativada",
                "O banimento da sua conta foi removido");

        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse grantAdminRole(UUID adminId, UUID targetUserId) {
        User user = findUserOrThrow(targetUserId);
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_ADMIN nao encontrada"));

        user.getRoles().add(adminRole);
        userRepository.save(user);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.ROLE_GRANTED, "USER", targetUserId,
                "Concedida a role ROLE_ADMIN");

        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse revokeAdminRole(UUID adminId, UUID targetUserId) {
        requireNotSelf(adminId, targetUserId, "Voce nao pode remover sua propria permissao de administrador");

        User user = findUserOrThrow(targetUserId);
        user.getRoles().removeIf(role -> role.getName() == RoleName.ROLE_ADMIN);
        userRepository.save(user);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.ROLE_REVOKED, "USER", targetUserId,
                "Removida a role ROLE_ADMIN");

        return toResponse(user);
    }

    private void auditAndNotify(UUID adminId, UUID targetUserId, User targetUser, AdminActionType action,
                                 String auditDetail, String notificationTitle, String notificationMessage) {
        adminAuditService.log(adminOrThrow(adminId), action, "USER", targetUserId, auditDetail);
        notificationService.notify(targetUser, NotificationType.OTHER, notificationTitle, notificationMessage, "USER", targetUserId);
    }

    private void requireNotSelf(UUID adminId, UUID targetUserId, String message) {
        if (adminId.equals(targetUserId)) {
            throw new BadRequestException(message);
        }
    }

    private User adminOrThrow(UUID adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador nao encontrado"));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getProfile() != null ? user.getProfile().getDisplayName() : user.getUsername(),
                user.getLevel(), user.getTotalXp(), user.isEnabled(), user.isBanned(),
                user.getSuspendedUntil(), user.getWarningCount(),
                user.getRoles().stream().map(r -> r.getName().name()).toList(),
                user.getLastLoginAt(), user.getCreatedAt());
    }
}
