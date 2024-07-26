package greencity.mapping;

import greencity.dto.ChatRoomDto;
import greencity.dto.ParticipantDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class that used by {@link ModelMapper} to map {@link ChatRoom} into
 * {@link ChatRoomDto}.
 */
@Component
@Transactional
public class ChatRoomDtoMapper extends AbstractConverter<ChatRoom, ChatRoomDto> {
    /**
     * Method convert {@link ChatRoom} to {@link ChatRoomDto}.
     *
     * @return {@link ChatRoomDto}
     */
    @Override
    protected ChatRoomDto convert(ChatRoom chatRoom) {
        Optional<ChatMessage> lastMessageOpt = getLastMassage(chatRoom);

        return ChatRoomDto.builder()
            .id(chatRoom.getId())
            .ownerId(chatRoom.getOwner().getId())
            .chatType(chatRoom.getType())
            .chatStatus(chatRoom.getChatStatus())
            .tariffId(chatRoom.getTariffId())
            .name(chatRoom.getName())
            .logo(chatRoom.getLogo())
            .participants(chatRoom.getParticipants().stream()
                .map(participant -> ParticipantDto.builder()
                    .id(participant.getId())
                    .name(participant.getName())
                    .email(participant.getEmail())
                    .role(participant.getRole())
                    .userStatus(participant.getUserStatus())
                    .profilePicture(participant.getProfilePicture())
                    .build())
                .collect(Collectors.toSet()))
            .lastMessage(lastMessageOpt.map(ChatMessage::getContent).orElse(null))
            .lastMessageDateTime(lastMessageOpt.map(ChatMessage::getCreateDate).orElse(null))
            .build();
    }

    private Optional<ChatMessage> getLastMassage(ChatRoom chatRoom) {
        return chatRoom.getMessages() != null ? chatRoom.getMessages().stream()
            .max(Comparator.comparing(ChatMessage::getCreateDate)) : Optional.empty();
    }
}
