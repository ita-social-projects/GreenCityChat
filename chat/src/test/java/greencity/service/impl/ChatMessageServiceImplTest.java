package greencity.service.impl;

import greencity.dto.ChatMessageDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {
    @InjectMocks
    private ChatMessageServiceImpl chatMessageServiceImpl;
    @Mock
    private ChatMessageRepo chatMessageRepo;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ChatRoomRepo chatRoomRepo;
    ChatMessageDto expectedChatMessageDto;
    ChatMessage expectedChatMessage;

    @BeforeEach
    void init() {
        expectedChatMessageDto = ChatMessageDto.builder()
            .roomId(1L)
            .senderId(1L)
            .content("test")
            .build();
    }

    @Test
    void findAllMessagesByChatRoomId() {
        Participant owner = Participant.builder()
            .id(1L)
            .build();
        ChatRoom chatRoom = ChatRoom.builder()
            .id(1L)
            .name("TestName")
            .owner(owner)
            .build();
        Optional<ChatRoom> roomOptional = Optional.of(chatRoom);
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .content("test")
            .room(chatRoom)
            .sender(owner)
            .build();
        List<ChatMessage> messages = Collections.singletonList(chatMessage);
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
            .id(1L)
            .content("test")
            .roomId(1L)
            .senderId(1L)
            .build();
        List<ChatMessageDto> chatMessageDtos = Collections.singletonList(chatMessageDto);

        when(chatRoomRepo.findById(1L)).thenReturn(roomOptional);
        when(chatMessageRepo.findAllByRoom(chatRoom)).thenReturn(messages);
        when(modelMapper.map(chatMessage, ChatMessageDto.class)).thenReturn(chatMessageDto);
        List<ChatMessageDto> actual = chatMessageServiceImpl.findAllMessagesByChatRoomId(1L);
        assertEquals(chatMessageDtos, actual);
    }

    @Test
    void processMessage() {
        when(modelMapper.map(expectedChatMessageDto, ChatMessage.class)).thenReturn(expectedChatMessage);
        when(chatMessageRepo.save(expectedChatMessage)).thenReturn(expectedChatMessage);
        chatMessageServiceImpl.processMessage(expectedChatMessageDto);
        verify(messagingTemplate).convertAndSend("/room/" + expectedChatMessageDto.getRoomId() + "/queue/messages",
            expectedChatMessageDto);

    }
}
