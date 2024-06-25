package greencity.mapping;

import greencity.dto.ChatMessageWithFileDto;
import greencity.entity.ChatMessage;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

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
            .build();
    }
}
