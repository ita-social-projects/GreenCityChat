package greencity.repository;


import greencity.entity.ChatMessage;
import greencity.entity.UnreadMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UnreadMessageRepo extends JpaRepository<UnreadMessage, Long>,
    JpaSpecificationExecutor<ChatMessage> {
}
