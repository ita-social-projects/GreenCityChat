package greencity.mapping;

import greencity.dto.ChatMessageWithFileDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * Class that used by {@link ModelMapper} to map {@link ChatMessageWithFileDto}
 * into {@link ChatMessage}.
 */
@Component
public class ChatMessageWithFileMapper extends AbstractConverter<ChatMessageWithFileDto, ChatMessage> {
    /**
     * Method convert {@link ChatMessageWithFileDto} to {@link ChatMessage}.
     *
     * @return {@link ChatMessage}
     */
    @Override
    protected ChatMessage convert(ChatMessageWithFileDto dto) {
        return ChatMessage.builder()
            .id(dto.getId())
            .content(dto.getContent())
            .sender(
                Participant.builder()
                    .id(dto.getSenderId()).build())
            .createDate(ZonedDateTime.now())
            .room(ChatRoom.builder()
                .id(dto.getRoomId()).build())
            .fileName(dto.getFileName())
            .fileType(dto.getFileType())
            .fileUrl(dto.getFileUrl())
            .build();
    }
}