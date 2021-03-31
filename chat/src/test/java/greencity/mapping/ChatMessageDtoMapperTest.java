package greencity.mapping;

import greencity.dto.ChatMessageDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatMessageDtoMapperTest {
    private ChatMessageDto expected;
    private ChatMessage chatMessage;
    private final ChatMessageDtoMapper chatMessageDtoMapper = new ChatMessageDtoMapper();

    @BeforeEach
    void init() {
        chatMessage = new ChatMessage(1L, new ChatRoom(1L, "name", null, null,
            ChatType.GROUP, null),
            new Participant(1L, "name", "asd@asd.asd", null,
                null, UserStatus.ACTIVATED, Role.ROLE_USER, null),
            "content", null, "fileName", "fileType", "fileUrl", null);
        expected = new ChatMessageDto(1L, 1L, 1L, "content",
            null, "fileName", "fileType", "fileUrl", null);
    }

    @Test
    void convert() {
        assertEquals(expected, chatMessageDtoMapper.convert(chatMessage));
    }
}
