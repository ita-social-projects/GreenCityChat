package greencity.mapping;

import greencity.dto.ChatMessageDto;
import greencity.dto.ChatRoomDto;
import greencity.dto.ParticipantDto;
import greencity.entity.ChatRoom;
import java.util.Objects;
import java.util.stream.Collectors;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ChatRoom} into
 * {@link ChatRoomDto}.
 */
@Component
public class ChatRoomDtoMapper extends AbstractConverter<ChatRoom, ChatRoomDto> {
    /**
     * Method convert {@link ChatRoom} to {@link ChatRoomDto}.
     *
     * @return {@link ChatRoomDto}
     */
    @Override
    protected ChatRoomDto convert(ChatRoom chatRoom) {
        if (chatRoom.getMessages() == null) {
            return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .ownerId(chatRoom.getOwner().getId())
                .chatType(chatRoom.getType())
                .name(chatRoom.getName())
                .logo(chatRoom.getLogo())
                .participants(chatRoom.getParticipants().stream().map(
                    participant -> ParticipantDto.builder()
                        .name(participant.getName())
                        .profilePicture(participant.getProfilePicture())
                        .id(participant.getId())
                        .email(participant.getEmail())
                        .role(participant.getRole())
                        .build())
                    .collect(Collectors.toSet()))
                .build();
        } else {
            return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .ownerId(chatRoom.getOwner().getId())
                .chatType(chatRoom.getType())
                .logo(chatRoom.getLogo())
////                getLogo.messages(chatRoom.getMessages().stream()
//                    .filter(Objects::nonNull)
//                    .map(
//                        chatMessage -> ChatMessageDto.builder()
//                            .id(chatMessage.getId())
//                            .content(chatMessage.getContent())
//                            .senderId(chatMessage.getSender().getId())
//                            .roomId(chatRoom.getId()).build())
//                    .collect(Collectors.toList()))
                    .name(chatRoom.getName())
                .participants(chatRoom.getParticipants().stream().map(
                    participant -> ParticipantDto.builder()
                        .name(participant.getName())
                        .profilePicture(participant.getProfilePicture())
                        .id(participant.getId())
                        .email(participant.getEmail())
                        .role(participant.getRole())
                        .build())
                    .collect(Collectors.toSet()))
                .build();
        }
    }
}
