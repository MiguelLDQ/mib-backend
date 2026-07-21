package com.mib.backend.service.impl;

import com.mib.backend.dto.response.ReportResponse;
import com.mib.backend.entity.ContentTargetType;
import com.mib.backend.entity.Report;
import com.mib.backend.entity.ReportReason;
import com.mib.backend.entity.User;
import com.mib.backend.exception.BadRequestException;
import com.mib.backend.exception.DuplicateReportException;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.ReportRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReportResponse createReport(UUID reporterId, String targetTypeRaw, UUID targetId,
                                        String reasonRaw, String description) {
        ContentTargetType targetType = parseEnum(targetTypeRaw, ContentTargetType.class, "tipo de alvo");
        ReportReason reason = parseEnum(reasonRaw, ReportReason.class, "motivo da denuncia");

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId)) {
            throw new DuplicateReportException("Voce ja denunciou este conteudo");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        Report saved = reportRepository.save(new Report(reporter, targetType, targetId, reason, description));

        return new ReportResponse(
                saved.getId(), saved.getTargetType().name(), saved.getTargetId(),
                saved.getReason().name(), saved.getDescription(), saved.getStatus().name(), saved.getCreatedAt());
    }

    private <E extends Enum<E>> E parseEnum(String raw, Class<E> enumType, String fieldLabel) {
        try {
            return Enum.valueOf(enumType, raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BadRequestException("Valor invalido para " + fieldLabel + ": " + raw);
        }
    }
}
