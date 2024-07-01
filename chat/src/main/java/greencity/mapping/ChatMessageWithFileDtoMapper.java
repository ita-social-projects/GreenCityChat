package greencity.mapping;

import greencity.dto.ChatMessageWithFileDto;
import greencity.dto.UserDto;
import greencity.entity.ChatMessage;
import greencity.entity.Participant;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link ChatMessage} into
 * {@link ChatMessageWithFileDto}.
 */
@Component
public class ChatMessageWithFileDtoMapper extends AbstractConverter<ChatMessage, ChatMessageWithFileDto> {
    /**
     * Method convert {@link ChatMessage} to {@link ChatMessageWithFileDto}.
     */
    @Override
    protected ChatMessageWithFileDto convert(ChatMessage chatMessage) {
        return ChatMessageWithFileDto.builder()
            .id(chatMessage.getId())
            .content(chatMessage.getContent())
            .createDate(chatMessage.getCreateDate())
            .senderId(chatMessage.getSender().getId())
            .roomId(chatMessage.getRoom().getId())
            .fileName(chatMessage.getFileName())
            .fileType(chatMessage.getFileType())
            .fileUrl(chatMessage.getFileUrl())
            .likes(chatMessage.getLikes() == null ? null
                : chatMessage.getLikes().stream()
                    .map(this::convertParticipant)
                    .collect(Collectors.toSet()))
            .build();
    }

    private UserDto convertParticipant(Participant participant) {
        return UserDto.builder()
            .id(participant.getId())
            .name(participant.getName())
            .profilePicture(participant.getProfilePicture())
            .build();
    }
}
