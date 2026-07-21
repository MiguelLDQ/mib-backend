package com.mib.backend.service.impl;

import com.mib.backend.dto.response.AdminLogResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.entity.AdminActionType;
import com.mib.backend.entity.AdminLog;
import com.mib.backend.entity.User;
import com.mib.backend.repository.AdminLogRepository;
import com.mib.backend.service.AdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuditServiceImpl implements AdminAuditService {

    private final AdminLogRepository adminLogRepository;

    @Override
    @Transactional
    public void log(User admin, AdminActionType action, String targetType, UUID targetId, String detail) {
        adminLogRepository.save(new AdminLog(admin, action, targetType, targetId, detail));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminLogResponse> list(int page, int size) {
        var result = adminLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return PagedResponse.from(result.map(l -> new AdminLogResponse(
                l.getId(), l.getAdmin().getUsername(), l.getAction().name(),
                l.getTargetType(), l.getTargetId(), l.getDetail(), l.getCreatedAt())));
    }
}
