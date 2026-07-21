package com.mib.backend.service;

import com.mib.backend.dto.request.MissionTemplateRequest;
import com.mib.backend.dto.response.MissionTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface AdminMissionService {

    List<MissionTemplateResponse> listAll();

    MissionTemplateResponse create(UUID adminId, MissionTemplateRequest request);

    MissionTemplateResponse update(UUID adminId, UUID templateId, MissionTemplateRequest request);

    void deactivate(UUID adminId, UUID templateId);
}
