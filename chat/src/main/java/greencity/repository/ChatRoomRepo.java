package greencity.repository;

import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * @param id {@link Long} id.
     * @return list of {@link ChatRoom} instances.
     */
    @Query(
        value = "SELECT room.* FROM chat_rooms room "
            + "INNER JOIN chat_rooms_participants crp on room.id = crp.room_id "
            + "WHERE crp.participant_id = :id",
        nativeQuery = true)
    List<ChatRoom> findAllByParticipant(@Param("id") Long id);

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
        + " AND cr.type = :chatType"
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
        + " AND cr.type = :chatType")
    List<ChatRoom> findChatRoomsByChatType(@Param("participant") Participant participant, @Param("chatType") ChatType chatType);

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
        + "values ( :chatroomid, :participantId)")
    void addUserToChatRoom(@Param("chatroomid") Long chatroomid, @Param("participantId") Long participantId);

    /**
     * {@inheritDoc}
     */
    @Query("select cr.participants from ChatRoom cr where cr.id = :id")
    Set<Participant> getParticipantsByChatRoomId(@Param("id") Long id);

    /**
     * {@inheritDoc}
     */
    @Query("SELECT COUNT(id) from UnreadMessage where participant.id = :userId and message.room.id = :roomId")
    Long countUnreadMessages(Long userId, Long roomId);

    /**
     * Method returns ids of chats between two people if exist.
     *
     */
    @Query(value = "SELECT DISTINCT room_id FROM chat_rooms_participants "
        + "where participant_id IN(:first, :second) "
        + "GROUP  BY room_id HAVING COUNT(room_id) = 2 ",
        nativeQuery = true)
    List<Long> chatExistBetweenTwo(@Param("first") Long firstUser, @Param("second") Long secondUser);

    /**
     * Retrieves all chat rooms associated with a specific tariff.
     *
     * @param tariffId the ID of the tariff
     * @return a list of chat rooms associated with the specified tariff
     */
    @Query("select cr from ChatRoom  cr where cr.tariffId = :tariffId")
    List<ChatRoom> findAllChatsByTariffId(Long tariffId);

    /**
     * Retrieves a page of chat rooms associated with a list of tariff IDs.
     *
     * @param tariffIds The list of tariff IDs for which to retrieve chat rooms.
     * @param pageable  The Pageable object that provides pagination information.
     * @return A page of chat rooms associated with the specified tariff IDs.
     */
    @Query("select cr from ChatRoom cr where cr.tariffId IN (:tariffIds)")
    Page<ChatRoom> findAllChatsByTariffIdPageable(List<Long> tariffIds, Pageable pageable);

    /**
     * Checks if a chat room exists with the provided userId and tariffId. If the
     * count is greater than 0, it returns true, indicating that a chat exists with
     * the provided userId and tariffId. Otherwise, it returns false.
     *
     * @param userId   The ID of the user.
     * @param tariffId The ID of the tariff.
     * @return true if a chat room exists with the provided userId and tariffId,
     *         false otherwise.
     */
    @Query(value = "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ChatRoom c "
        + "JOIN c.participants p WHERE p.id = :userId AND c.tariffId = :tariffId")
    boolean existsByUserIdAndTariffId(@Param("userId") Long userId, @Param("tariffId") Long tariffId);

    /**
     * Finds a chat room with the provided userId and tariffId.
     *
     * @param userId The ID of the user.
     * @return The ChatRoom entity if a chat room exists with the provided userId
     *         and tariffId, null otherwise.
     */
    @Query("SELECT c FROM ChatRoom c JOIN c.participants p WHERE p.id = :userId AND c.tariffId = :tariffId")
    ChatRoom findByUserIdAndTariffId(@Param("userId") Long userId, @Param("tariffId") Long tariffId);

    /**
     * Retrieves a page of all chat rooms.
     *
     * @param pageable Pagination information.
     * @return A page of chat rooms.
     */
    @Query("SELECT c FROM ChatRoom c")
    Page<ChatRoom> findAll(Pageable pageable);
}
