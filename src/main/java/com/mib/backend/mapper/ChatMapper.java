package com.mib.backend.mapper;

import com.mib.backend.dto.response.MessageResponse;
import com.mib.backend.entity.Message;
import com.mib.backend.entity.ShopItemType;
import com.mib.backend.repository.UserInventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final UserInventoryItemRepository inventoryItemRepository;

    public MessageResponse toResponse(Message message) {
        var sender = message.getSender();
        var profile = sender.getProfile();

        String equippedTitle = inventoryItemRepository.findEquippedByUserIdAndType(sender.getId(), ShopItemType.TITLE)
                .stream().findFirst().map(item -> item.getShopItem().getName()).orElse(null);
        String equippedFrameUrl = inventoryItemRepository.findEquippedByUserIdAndType(sender.getId(), ShopItemType.FRAME)
                .stream().findFirst().map(item -> item.getShopItem().getIconUrl()).orElse(null);

        return new MessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                sender.getId(),
                sender.getUsername(),
                profile != null ? profile.getDisplayName() : sender.getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                equippedTitle,
                equippedFrameUrl,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
