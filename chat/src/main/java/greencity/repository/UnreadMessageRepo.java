package greencity.repository;

import greencity.entity.ChatMessage;
import greencity.entity.UnreadMessage;
import java.util.List;
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
}
