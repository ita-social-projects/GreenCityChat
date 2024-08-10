package greencity.mapping;

import greencity.dto.ChatRoomDto;
import greencity.dto.ParticipantDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ChatRoomMapperTest {
    private ChatRoomDto chatRoomDto;
    private ChatRoom chatRoom;
    private ChatRoomMapper chatRoomMapper = new ChatRoomMapper();

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
                .build();
    }
    @Test
    void convert() {
        ChatRoom actual = chatRoomMapper.convert(chatRoomDto);
        assertEquals(chatRoom.getId(), actual.getId());
        assertEquals(chatRoom.getName(), actual.getName());
        assertEquals(chatRoom.getType(), actual.getType());
        assertEquals(chatRoom.getOwner().getId(), actual.getOwner().getId());
        assertEquals(chatRoomDto.getParticipants().toArray().length, actual.getParticipants().toArray().length);
        assertEquals(chatRoom.getChatStatus(), actual.getChatStatus());
        assertEquals(chatRoom.getTariffId(), actual.getTariffId());
        assertEquals(chatRoom.getLogo(), actual.getLogo());
    }
}