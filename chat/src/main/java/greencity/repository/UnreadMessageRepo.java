package greencity.repository;

import greencity.entity.ChatMessage;
import greencity.entity.UnreadMessage;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UnreadMessageRepo extends JpaRepository<UnreadMessage, Long>,
    JpaSpecificationExecutor<ChatMessage> {
    /**
     * {@inheritDoc}
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "delete from unread_messages where user_id = :userId and message_id in(:messageIds)")
    void cleanUnreadMessage(Long userId, List<Long> messageIds);

    /**
     * Method to delete unread message by message id.
     *
     * @param messageId {@link Long} id of message.
     */
    @Transactional
    void deleteByMessageId(Long messageId);

    /**
     * Method to find all unread message by user id.
     *
     * @param userId {@link Long} id of user.
     */
    @Transactional
    @Query(nativeQuery = true, value = "select um.message_id from unread_messages um "
        + "where um.user_id = :userId")
    Set<Long> findUnreadMessagesIdByUserId(Long userId);
}
