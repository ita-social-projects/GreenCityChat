package greencity.repository;

import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ChatRoomRepo extends JpaRepository<ChatRoom, Long>,
    JpaSpecificationExecutor<ChatRoom> {
    /**
     * Method to find all {@link ChatRoom}'s by {@link Participant}/{@code User} id.
     *
     * @param participant {@link Participant} id.
     * @return list of {@link ChatRoom} instances.
     */
    @Query(value = "SELECT room FROM ChatRoom room"
        + " WHERE :part IN elements(room.participants)")
    List<ChatRoom> findAllByParticipant(@Param("part") Participant participant);

    /**
     * Method to find all {@link ChatRoom}'s by {@link Participant}/{@code User}'s
     * and {@link ChatType}.
     *
     * @param participants      {@link Set} of {@link Participant}'s that are in
     *                          certain rooms.
     * @param participantsCount participants count from passed {@link Set}.
     * @param chatType          {@link ChatType} room type.
     * @return list of {@link ChatRoom} instances.
     */
    @Query(value = "SELECT cr FROM ChatRoom cr"
        + " JOIN cr.participants p"
        + " WHERE p IN :participants"
        + " AND UPPER(cr.type) = :chatType"
        + " GROUP BY cr.id"
        + " HAVING COUNT(cr.id) = CAST(:participantsCount AS long)")
    List<ChatRoom> findByParticipantsAndStatus(@Param("participants") Set<Participant> participants,
                                               @Param("participantsCount") Integer participantsCount,
                                               @Param("chatType") ChatType chatType);

    /**
     * {@inheritDoc}
     */
    @Query(value = "SELECT cr FROM ChatRoom cr"
        + " JOIN cr.participants p"
        + " WHERE p IN :participant"
        + " AND cr.messages IS NOT EMPTY"
        + " AND UPPER(cr.type) = :chatType")
    List<ChatRoom> findGroupChats(@Param("participant") Participant participant, @Param("chatType") ChatType chatType);

    /**
     * {@inheritDoc}
     */
    @Query(value = "SELECT cr FROM ChatRoom cr "
        + "JOIN cr.participants p "
        + "WHERE LOWER(cr.name) "
        + "LIKE LOWER(concat(?1, '%')) "
        + "AND p IN ?2")
    List<ChatRoom> findAllChatRoomsByQuery(String query, Participant participant);

    /**
     * Method select all system chat.
     */
    @Query("select cr from ChatRoom  cr where cr.type = 'SYSTEM'")
    List<ChatRoom> findSystemChatRooms();

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "insert into chat_rooms_participants(room_id,participant_id)"
        + "values ( :chatroomid, :prticipantid )")
    void addUserToSystemChatRoom(@Param("chatroomid") Long chatroomid, @Param("prticipantid") Long prticipantid);

    /**
     * {@inheritDoc}
     */
    @Query("select cr.participants from ChatRoom cr where cr.id = :id")
    Set<Participant> getPatricipantsByChatRoomId(@Param("id") Long id);

    @Query("SELECT COUNT(id) from UnreadMessage where participant.id = :userId and message.room.id = :roomId")
    Long countUnreadMessages(Long userId, Long roomId);
}
