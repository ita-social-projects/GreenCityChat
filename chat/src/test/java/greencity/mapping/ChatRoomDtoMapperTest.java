package greencity.mapping;

import greencity.dto.ChatMessageDto;
import greencity.dto.ChatRoomDto;
import greencity.dto.ParticipantDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.enums.Role;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatRoomDtoMapperTest {
    private ChatRoomDto chatRoomDto;
    private ChatRoom chatRoom;
    private ChatRoomDtoMapper chatRoomDtoMapper = new ChatRoomDtoMapper();

    @BeforeEach
    void init() {
        Participant participant = Participant.builder()
            .id(1L)
            .email("asd@asd.asd")
            .role(Role.ROLE_USER)
            .profilePicture("asd")
            .build();
        chatRoom = ChatRoom.builder()
            .id(1L)
            .messages(Arrays.asList(ChatMessage.builder().id(1L).sender(Participant.builder()
                .id(1L).build()).build()))
            .name("Name")
            .participants(Set.of(participant))
            .owner(participant)
            .type(ChatType.GROUP)
            .build();
        chatRoomDto = ChatRoomDto.builder()
            .id(1L)
            .ownerId(1L)
            .chatType(ChatType.GROUP)
            .name("Name")
            .participants(chatRoom.getParticipants().stream().map(
                participantt -> ParticipantDto.builder()
                    .name(participantt.getName())
                    .profilePicture(participantt.getProfilePicture())
                    .id(participantt.getId())
                    .email(participantt.getEmail())
                    .role(participantt.getRole())
                    .build())
                .collect(Collectors.toSet()))
            .messages(chatRoom.getMessages().stream()
                .map(chatMessage -> ChatMessageDto.builder()
                    .id(chatMessage.getId())
                    .content(chatMessage.getContent())
                    .fileName(chatMessage.getFileName())
                    .fileType(chatMessage.getFileType())
                    .senderId(chatMessage.getSender().getId())
                    .roomId(chatRoom.getId())
                    .build())
                .collect(
                    Collectors.toList()))
            .build();
    }

    @Test
    void convert() {
        ChatRoomDto actual = chatRoomDtoMapper.convert(chatRoom);
        assertEquals(chatRoomDto.getId(), actual.getId());
        assertEquals(chatRoomDto.getChatType(), actual.getChatType());
        assertEquals(chatRoomDto.getName(), actual.getName());
        assertEquals(chatRoomDto.getOwnerId(), actual.getOwnerId());
        assertArrayEquals(chatRoomDto.getParticipants().toArray(), actual.getParticipants().toArray());
        assertArrayEquals(chatRoomDto.getMessages().toArray(), actual.getMessages().toArray());
    }

    @Test
    void convertWithNullMessages() {
        chatRoom.setMessages(null);
        ChatRoomDto actual = chatRoomDtoMapper.convert(chatRoom);
        assertEquals(chatRoomDto.getId(), actual.getId());
        assertEquals(chatRoomDto.getChatType(), actual.getChatType());
        assertEquals(chatRoomDto.getName(), actual.getName());
        assertEquals(chatRoomDto.getOwnerId(), actual.getOwnerId());
        assertArrayEquals(chatRoomDto.getParticipants().toArray(), actual.getParticipants().toArray());
    }

}