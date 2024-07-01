package greencity.mapping;

import greencity.dto.ChatMessageWithFileDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}