package com.mib.backend.service.impl;

import com.mib.backend.dto.request.MissionTemplateRequest;
import com.mib.backend.dto.response.MissionTemplateResponse;
import com.mib.backend.entity.AdminActionType;
import com.mib.backend.entity.MissionCategory;
import com.mib.backend.entity.MissionDifficulty;
import com.mib.backend.entity.MissionTemplate;
import com.mib.backend.exception.BadRequestException;
import com.mib.backend.exception.MissionNotFoundException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.MissionTemplateRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AdminAuditService;
import com.mib.backend.service.AdminMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMissionServiceImpl implements AdminMissionService {

    private final MissionTemplateRepository missionTemplateRepository;
    private final UserRepository userRepository;
    private final AdminAuditService adminAuditService;

    @Override
    @Transactional(readOnly = true)
    public List<MissionTemplateResponse> listAll() {
        return missionTemplateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public MissionTemplateResponse create(UUID adminId, MissionTemplateRequest request) {
        MissionTemplate template = new MissionTemplate(
                request.title(), request.description(),
                parseCategory(request.category()), parseDifficulty(request.difficulty()), request.baseXpReward());

        MissionTemplate saved = missionTemplateRepository.save(template);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.MISSION_TEMPLATE_CREATED,
                "MISSION_TEMPLATE", saved.getId(), "Template criado: " + saved.getTitle());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public MissionTemplateResponse update(UUID adminId, UUID templateId, MissionTemplateRequest request) {
        MissionTemplate template = findOrThrow(templateId);

        template.setTitle(request.title());
        template.setDescription(request.description());
        template.setCategory(parseCategory(request.category()));
        template.setDifficulty(parseDifficulty(request.difficulty()));
        template.setBaseXpReward(request.baseXpReward());

        MissionTemplate saved = missionTemplateRepository.save(template);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.MISSION_TEMPLATE_UPDATED,
                "MISSION_TEMPLATE", saved.getId(), "Template atualizado: " + saved.getTitle());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivate(UUID adminId, UUID templateId) {
        MissionTemplate template = findOrThrow(templateId);
        template.setActive(false);
        missionTemplateRepository.save(template);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.MISSION_TEMPLATE_DEACTIVATED,
                "MISSION_TEMPLATE", templateId, "Template desativado: " + template.getTitle());
    }

    private MissionTemplate findOrThrow(UUID id) {
        return missionTemplateRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Template de missao nao encontrado"));
    }

    private MissionCategory parseCategory(String raw) {
        try {
            return MissionCategory.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Categoria de missao invalida: " + raw);
        }
    }

    private MissionDifficulty parseDifficulty(String raw) {
        try {
            return MissionDifficulty.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Dificuldade de missao invalida: " + raw);
        }
    }

    private MissionTemplateResponse toResponse(MissionTemplate t) {
        return new MissionTemplateResponse(
                t.getId(), t.getTitle(), t.getDescription(), t.getCategory().name(),
                t.getDifficulty().name(), t.getBaseXpReward(), t.isActive());
    }

    private com.mib.backend.entity.User adminOrThrow(UUID adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador nao encontrado"));
    }
}
