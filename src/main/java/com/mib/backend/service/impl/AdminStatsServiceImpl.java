package com.mib.backend.service.impl;

import com.mib.backend.dto.response.PlatformStatsResponse;
import com.mib.backend.entity.ReportStatus;
import com.mib.backend.entity.TaskStatus;
import com.mib.backend.repository.AnonymousMessageRepository;
import com.mib.backend.repository.BreathingSessionLogRepository;
import com.mib.backend.repository.MessageRepository;
import com.mib.backend.repository.ReportRepository;
import com.mib.backend.repository.TaskRepository;
import com.mib.backend.repository.UserMissionCompletionRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final AnonymousMessageRepository anonymousMessageRepository;
    private final TaskRepository taskRepository;
    private final UserMissionCompletionRepository userMissionCompletionRepository;
    private final BreathingSessionLogRepository breathingSessionLogRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public PlatformStatsResponse getPlatformStats() {
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        return new PlatformStatsResponse(
                userRepository.count(),
                userRepository.countByBannedTrue(),
                userRepository.countBySuspendedUntilAfter(now),
                userRepository.countByCreatedAtAfter(sevenDaysAgo),
                userRepository.countByCreatedAtAfter(thirtyDaysAgo),
                messageRepository.countByRemovedByModerationFalse(),
                anonymousMessageRepository.countByRemovedByModerationFalseAndRemovedByAuthorFalse(),
                taskRepository.countByStatus(TaskStatus.COMPLETED),
                userMissionCompletionRepository.count(),
                breathingSessionLogRepository.count(),
                reportRepository.countByStatus(ReportStatus.PENDING)
        );
    }
}
