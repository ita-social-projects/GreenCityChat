package greencity.service;

import greencity.dto.ChatMessageDto;
import greencity.dto.PageableDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatMessageService {
    /**
     * Method to find all {@link ChatMessage}'s by {@link ChatRoom} id.
     *
     * @param chatRoomId {@link ChatMessage} id.
     * @return list of {@link ChatMessage} instances.
     */
    PageableDto<ChatMessageDto> findAllMessagesByChatRoomId(Long chatRoomId, Pageable pageable);


    /**
     * Method to process all {@link ChatMessageDto}'s that are sent from client
     * side.
     *
     * @param chatMessage {@link ChatMessageDto} chatMessage.
     */
    void processMessage(ChatMessageDto chatMessage);

    /**
     * {@inheritDoc}
     */
    void deleteMessage(ChatMessageDto chatMessage);

    /**
     * {@inheritDoc}
     */
    void updateMessage(ChatMessageDto chatMessageDto);

    /**
     * {@inheritDoc}
     */
    ChatMessageDto findTopByOrderByIdDesc();
}
