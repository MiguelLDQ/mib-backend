package com.mib.backend.service.impl;

import com.mib.backend.dto.request.UpdateInterestsRequest;
import com.mib.backend.dto.response.InterestResponse;
import com.mib.backend.entity.ChatRoomParticipant;
import com.mib.backend.entity.Interest;
import com.mib.backend.entity.User;
import com.mib.backend.entity.UserInterest;
import com.mib.backend.exception.ResourceNotFoundException;
import com.mib.backend.repository.ChatRoomParticipantRepository;
import com.mib.backend.repository.InterestRepository;
import com.mib.backend.repository.UserInterestRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InterestResponse> getCatalog(UUID currentUserId) {
        Set<UUID> selected = userInterestRepository.findAllByUserId(currentUserId).stream()
                .map(ui -> ui.getInterest().getId())
                .collect(Collectors.toSet());

        return interestRepository.findAllByOrderByNameAsc().stream()
                .map(interest -> new InterestResponse(
                        interest.getId(), interest.getName(), interest.getIconName(),
                        selected.contains(interest.getId())))
                .toList();
    }

    @Override
    @Transactional
    public List<InterestResponse> updateMyInterests(UUID currentUserId, UpdateInterestsRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        List<UserInterest> current = userInterestRepository.findAllByUserId(currentUserId);
        Set<UUID> currentInterestIds = current.stream().map(ui -> ui.getInterest().getId()).collect(Collectors.toSet());
        Set<UUID> newInterestIds = Set.copyOf(request.interestIds());

        // Sai das salas tematicas dos interesses removidos
        current.stream()
                .filter(ui -> !newInterestIds.contains(ui.getInterest().getId()))
                .forEach(ui -> leaveThemeRoomIfAny(currentUserId, ui.getInterest()));

        userInterestRepository.deleteAllByUserId(currentUserId);

        List<Interest> interestsToAdd = interestRepository.findAllById(newInterestIds);
        interestsToAdd.forEach(interest -> {
            userInterestRepository.save(new UserInterest(user, interest));
            if (!currentInterestIds.contains(interest.getId())) {
                joinThemeRoomIfAny(user, interest);
            }
        });

        return getCatalog(currentUserId);
    }

    private void joinThemeRoomIfAny(User user, Interest interest) {
        if (interest.getThemeRoom() == null) {
            return;
        }
        boolean alreadyParticipant = chatRoomParticipantRepository
                .existsByChatRoomIdAndUserId(interest.getThemeRoom().getId(), user.getId());
        if (!alreadyParticipant) {
            chatRoomParticipantRepository.save(new ChatRoomParticipant(interest.getThemeRoom(), user));
        }
    }

    private void leaveThemeRoomIfAny(UUID userId, Interest interest) {
        if (interest.getThemeRoom() == null) {
            return;
        }
        chatRoomParticipantRepository.findAllByChatRoomId(interest.getThemeRoom().getId()).stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .ifPresent(chatRoomParticipantRepository::delete);
    }
}
