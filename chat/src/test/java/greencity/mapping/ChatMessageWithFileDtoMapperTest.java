package greencity.mapping;

import greencity.dto.ChatMessageWithFileDto;
import greencity.dto.UserDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatMessageWithFileDtoMapperTest {
    private ChatMessageWithFileDto expected;
    private ChatMessage chatMessage;
    private final ChatMessageWithFileDtoMapper chatMessageWithFileDtoMapper = new ChatMessageWithFileDtoMapper();

    @BeforeEach
    void init() {
        chatMessage = new ChatMessage(1L, new ChatRoom(1L, "name", null, null,
            ChatType.GROUP, null, null),
            new Participant(1L, "name", "asd@asd.asd", null,
                null, UserStatus.ACTIVATED, Role.ROLE_USER, null),
            "content", null, null, "fileName", "AUDIO",
            "https://example.wav", null);
        expected = new ChatMessageWithFileDto(1L, 1L, 1L, "content",
            null, "fileName", "AUDIO", "https://example.wav", null);
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
        chatMessage.setFileName(null);
        chatMessage.setFileType(null);
        chatMessage.setFileUrl(null);

        expected.setFileName(null);
        expected.setFileType(null);
        expected.setFileUrl(null);

        assertEquals(expected, chatMessageWithFileDtoMapper.convert(chatMessage));
    }
}