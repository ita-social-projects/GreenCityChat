package greencity.service;

import greencity.dto.ChatMessageDto;
import greencity.dto.FriendsChatDto;
import greencity.dto.MessageLike;
import greencity.dto.PageableDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import org.springframework.data.domain.Pageable;

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

    /**
     * Method to like message.
     *
     * @param messageLike {@link MessageLike}.
     */
    void likeMessage(MessageLike messageLike);

    /**
     * {@inheritDoc}
     */
    void cleanUnreadMessages(Long userId, Long roomId);

    /**
     * {@inheritDoc}
     */
    ChatMessageDto sentMessage(Long userId, Long chatId, String content);

    /**
     * Method return if there is already created conversation between two users.
     *
     * @return {@link Boolean}.
     */
    FriendsChatDto chatExist(Long fistUserId, Long secondUserId);
}
