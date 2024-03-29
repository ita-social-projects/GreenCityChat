package greencity.service;

import greencity.dto.ChatRoomDto;
import greencity.dto.GroupChatRoomCreateDto;
import greencity.dto.LeaveChatDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;

import java.util.List;
import java.util.Set;

public interface ChatRoomService {
    /**
     * Method to find all {@link ChatRoom}'s by {@link Participant}/{@code User} id.
     *
     * @param name {@link String} name(email) of user.
     * @return {@link ChatRoom} instance.
     */
    List<ChatRoomDto> findAllByParticipantName(String name);

    /**
     * Method to find all {@link ChatRoom}'s by {@link Participant}/{@code User} and
     * {@link ChatType}.
     *
     * @param participants {@link Set} of {@link Participant}'s that are in certain
     *                     rooms.
     * @param chatType     {@link ChatType} room type.
     * @return list of {@link ChatRoom} instances.
     */
    List<ChatRoomDto> findAllRoomsByParticipantsAndStatus(Set<Participant> participants, ChatType chatType);

    /**
     * Method to find {@link ChatRoom} by it's id.
     *
     * @param id {@link ChatRoom} id.
     * @return {@link ChatRoom} instance.
     */
    ChatRoomDto findChatRoomById(Long id);

    /**
     * {@inheritDoc}
     */
    ChatRoomDto findPrivateByParticipants(Long id, String name);

    /**
     * {@inheritDoc}
     */
    List<ChatRoomDto> findGroupByParticipants(List<Long> id, String name, String chatName);

    /**
     * {@inheritDoc}
     */
    List<ChatRoomDto> findGroupChatRooms(Participant participant, ChatType chatType);

    /**
     * {@inheritDoc}
     */
    List<ChatRoomDto> findAllChatRoomsByQuery(String query, Participant participant);

    /**
     * {@inheritDoc}
     */
    List<ChatRoomDto> findAllVisibleRooms(String name);

    /**
     * {@inheritDoc}
     */
    Long addNewUserToSystemChat(Long userId);

    /**
     * {@inheritDoc}
     */
    ChatRoomDto createNewChatRoom(GroupChatRoomCreateDto dto);

    /**
     * {@inheritDoc}
     */
    void deleteParticipantsFromChatRoom(ChatRoomDto chatRoomDto);

    /**
     * {@inheritDoc}
     */
    void updateChatRoom(ChatRoomDto chatRoomDto);

    /**
     * {@inheritDoc}
     */
    void deleteChatRoom(Long ownerId, ChatRoomDto chatRoomDto);

    /**
     * {@inheritDoc}
     */
    void leaveChatRoom(LeaveChatDto leaveChatDto);

    /**
     * {@inheritDoc}
     */
    void findPrivateByParticipantsForSockets(Long id, Long currentUserId);

    /**
     * Method deletes all {@link ChatMessage}s from chatroom.
     *
     * @param roomId {@link Long} chatroom's id
     * @param userId {@link Long} user's id
     */
    void deleteMessagesFromChatRoom(Long roomId, Long userId);
}
