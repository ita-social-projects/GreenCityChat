package greencity.mapping;

import greencity.dto.ChatMessageDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatMessageMapperTest {
    private ChatMessage expected;
    private ChatMessageDto chatMessageDto;
    private ChatMessageMapper chatMessageMapper = new ChatMessageMapper();

    @BeforeEach
    void init() {
        expected = new ChatMessage(1L, ChatRoom.builder().id(1L).build(),
            Participant.builder().id(1L).build(),
            "content", ZonedDateTime.now(), "fileName", "fileType", "fileUrl", null);
        chatMessageDto = new ChatMessageDto(1L, 1L, 1L, "content",
            null, "fileName", "fileType", "fileUrl", null);
    }

    @Test
    void convert() {
        ChatMessage actual = chatMessageMapper.convert(chatMessageDto);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getContent(), actual.getContent());
        assertEquals(expected.getFileName(), actual.getFileName());
        assertEquals(expected.getFileType(), actual.getFileType());
        assertEquals(expected.getSender(), actual.getSender());
        assertEquals(expected.getRoom(), actual.getRoom());
        assertEquals(expected.getFileUrl(), actual.getFileUrl());
    }
}
