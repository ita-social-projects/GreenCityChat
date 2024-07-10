package greencity.mapping;

import greencity.dto.ChatMessageWithFileDto;
import greencity.dto.UserDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatStatus;
import greencity.enums.ChatType;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageWithFileDtoMapperTest {
    private ChatMessageWithFileDto expected;
    private ChatMessage chatMessage;
    private final ChatMessageWithFileDtoMapper chatMessageWithFileDtoMapper = new ChatMessageWithFileDtoMapper();

    @BeforeEach
    void init() {
        chatMessage = new ChatMessage(1L, new ChatRoom(1L, "name", null, null,
            ChatType.GROUP, ChatStatus.NEW, 1L, null, "logo"),
            new Participant(1L, "name", "asd@asd.asd", null,
                null, UserStatus.ACTIVATED, Role.ROLE_USER, null),
            "content", null, null, "fileName", "AUDIO",
            "https://example.wav", null);
        expected = new ChatMessageWithFileDto(1L, 1L, 1L, "content",
            null, "fileName", "AUDIO", "https://example.wav", null, false);
    }

    @Test
    void convert() {
        assertEquals(expected, chatMessageWithFileDtoMapper.convert(chatMessage));
    }

    @Test
    void convertWithLikes() {
        Set<Participant> likes = new HashSet<>();
        likes.add(new Participant(2L, "user2", "user2@example.com", null,
            null, UserStatus.ACTIVATED, Role.ROLE_USER, null));
        chatMessage.setLikes(likes);

        Set<UserDto> expectedLikes = new HashSet<>();
        expectedLikes.add(new UserDto(2L, "user2", null));
        expected.setLikes(expectedLikes);

        assertEquals(expected, chatMessageWithFileDtoMapper.convert(chatMessage));
    }

    @Test
    void convertWithEmptyLikes() {
        chatMessage.setLikes(Collections.emptySet());
        expected.setLikes(Collections.emptySet());

        assertEquals(expected, chatMessageWithFileDtoMapper.convert(chatMessage));
    }

    @Test
    void convertWithNullFields() {
        ChatMessage message = new ChatMessage(1L, new ChatRoom(1L, "name", null, null,
            ChatType.GROUP, ChatStatus.NEW, 1L, null, "logo"),
            new Participant(1L, "name", "asd@asd.asd", null,
                null, UserStatus.ACTIVATED, Role.ROLE_USER, null),
            "content", null, null, null, null,
            null, null);
        ChatMessageWithFileDto expectedDto = new ChatMessageWithFileDto(1L, 1L, 1L, "content",
            null, null, null, null, null, false);

        assertEquals(expectedDto, chatMessageWithFileDtoMapper.convert(message));
    }

}