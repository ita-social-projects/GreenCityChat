package greencity.mapping;

import greencity.dto.ChatMessageWithFileDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatMessageWithFileMapperTest {
    private ChatMessage expected;
    private ChatMessageWithFileDto chatMessageWithFileDto;
    private ChatMessageWithFileMapper chatMessageWithFileMapper = new ChatMessageWithFileMapper();

    @BeforeEach
    void init() {
        expected = new ChatMessage(1L, ChatRoom.builder().id(1L).build(),
            Participant.builder().id(1L).build(),
            "content", ZonedDateTime.now(), null, "fileName", "AUDIO",
            "https://example.wav");
        chatMessageWithFileDto = new ChatMessageWithFileDto(1L, 1L, 1L, "content",
            null, "fileName", "AUDIO", "https://example.wav");
    }

    @Test
    void convert() {
        ChatMessage actual = chatMessageWithFileMapper.convert(chatMessageWithFileDto);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getContent(), actual.getContent());
        assertEquals(expected.getSender(), actual.getSender());
        assertEquals(expected.getRoom(), actual.getRoom());
        assertEquals(expected.getFileName(), actual.getFileName());
        assertEquals(expected.getFileType(), actual.getFileType());
        assertEquals(expected.getFileUrl(), actual.getFileUrl());
    }
}