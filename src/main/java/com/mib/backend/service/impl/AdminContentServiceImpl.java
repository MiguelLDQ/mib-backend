package com.mib.backend.service.impl;

import com.mib.backend.dto.response.ModerationLogResponse;
import com.mib.backend.dto.response.PagedResponse;
import com.mib.backend.dto.response.ReportResponse;
import com.mib.backend.entity.AdminActionType;
import com.mib.backend.entity.AnonymousMessage;
import com.mib.backend.entity.Message;
import com.mib.backend.entity.Report;
import com.mib.backend.entity.ReportStatus;
import com.mib.backend.exception.AnonymousMessageNotFoundException;
import com.mib.backend.exception.BadRequestException;
import com.mib.backend.exception.MessageNotFoundException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.AnonymousMessageRepository;
import com.mib.backend.repository.MessageRepository;
import com.mib.backend.repository.ModerationLogRepository;
import com.mib.backend.repository.ReportRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AdminAuditService;
import com.mib.backend.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final MessageRepository messageRepository;
    private final AnonymousMessageRepository anonymousMessageRepository;
    private final ReportRepository reportRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final UserRepository userRepository;
    private final AdminAuditService adminAuditService;

    @Override
    @Transactional
    public void deleteChatMessage(UUID adminId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Mensagem nao encontrada"));

        message.setRemovedByModeration(true);
        messageRepository.save(message);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.MESSAGE_DELETED,
                "CHAT_MESSAGE", messageId, "Mensagem removida por administrador");
    }

    @Override
    @Transactional
    public void deleteAnonymousMessage(UUID adminId, UUID messageId) {
        AnonymousMessage message = anonymousMessageRepository.findById(messageId)
                .orElseThrow(() -> new AnonymousMessageNotFoundException("Mensagem nao encontrada"));

        message.setRemovedByModeration(true);
        anonymousMessageRepository.save(message);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.ANONYMOUS_MESSAGE_DELETED,
                "ANONYMOUS_MESSAGE", messageId, "Mensagem anonima removida por administrador");
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReportResponse> listReports(String status, int page, int size) {
        ReportStatus reportStatus = (status == null || status.isBlank())
                ? ReportStatus.PENDING : parseReportStatus(status);

        var result = reportRepository.findByStatusOrderByCreatedAtDesc(reportStatus, PageRequest.of(page, size));
        return PagedResponse.from(result.map(this::toReportResponse));
    }

    @Override
    @Transactional
    public ReportResponse reviewReport(UUID adminId, UUID reportId, String status) {
        ReportStatus newStatus = parseReportStatus(status);
        if (newStatus == ReportStatus.PENDING) {
            throw new BadRequestException("Use REVIEWED ou DISMISSED para revisar uma denuncia");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia nao encontrada"));

        report.setStatus(newStatus);
        report.setReviewedAt(Instant.now());
        reportRepository.save(report);

        adminAuditService.log(adminOrThrow(adminId), AdminActionType.REPORT_REVIEWED,
                report.getTargetType().name(), report.getTargetId(),
                "Denuncia marcada como " + newStatus.name());

        return toReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ModerationLogResponse> listModerationLogs(int page, int size) {
        var result = moderationLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return PagedResponse.from(result.map(log -> new ModerationLogResponse(
                log.getId(), log.getUser().getId(), log.getUser().getUsername(),
                log.getTargetType().name(), log.getTargetId(), log.getCategory().name(),
                log.getAction().name(), log.getDetail(), log.getCreatedAt())));
    }

    private ReportStatus parseReportStatus(String raw) {
        try {
            return ReportStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Status de denuncia invalido: " + raw);
        }
    }

    private ReportResponse toReportResponse(Report report) {
        return new ReportResponse(
                report.getId(), report.getTargetType().name(), report.getTargetId(),
                report.getReason().name(), report.getDescription(), report.getStatus().name(), report.getCreatedAt());
    }

    private com.mib.backend.entity.User adminOrThrow(UUID adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador nao encontrado"));
    }
}
