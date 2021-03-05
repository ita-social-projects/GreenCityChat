package greencity.service;

import greencity.dto.ChatMessageDto;
import greencity.dto.ChatRoomDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;
import greencity.service.impl.ChatMessageServiceImpl;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {
    @InjectMocks
    private ChatMessageServiceImpl chatMessageServiceImpl;
    @Mock
    private ChatMessageRepo chatMessageRepo;
    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ChatRoomRepo chatRoomRepo;
    ChatRoomDto expectedChatRoomDto;
    List<ChatMessage> expectedChatMessagesList;
    ChatRoom expectedChatRoom;
    List<ChatMessageDto> expectedChatMessageDtoList;
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
        List<Long> userLike = Collections.singletonList(1L);
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
            .id(1L)
            .content("test")
            .roomId(1L)
            .senderId(1L)
            .build();
        chatMessageDto.setLikedUserId(userLike);
        List<ChatMessageDto> chatMessageDtos = Collections.singletonList(chatMessageDto);

        when(chatRoomRepo.findById(1L)).thenReturn(roomOptional);
        when(chatMessageRepo.findAllByRoom(chatRoom)).thenReturn(messages);
        when(modelMapper.map(chatMessage, ChatMessageDto.class)).thenReturn(chatMessageDto);
        when(chatMessageRepo.getLikesByMessageId(1L)).thenReturn(userLike);
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
