package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.ChatMessageDto;
import greencity.dto.ChatRoomDto;
import greencity.dto.MessageLike;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.exception.exceptions.ChatRoomNotFoundException;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;
import greencity.service.impl.ChatMessageServiceImpl;
import java.time.ZonedDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

        expectedChatMessage = ChatMessage.builder()
            .id(1L)
            .content("content")
            .sender(new Participant())
            .room(new ChatRoom())
            .createDate(ZonedDateTime.now())
            .fileName("filename")
            .fileType("fileType")
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
    public void findAllMessagesByChatRoomIdFailed() {
        when(chatRoomRepo.findById(anyLong()))
            .thenThrow(new ChatRoomNotFoundException(ErrorMessage.CHAT_ROOM_NOT_FOUND_BY_ID));

        assertThrows(ChatRoomNotFoundException.class,
            () -> chatMessageServiceImpl.findAllMessagesByChatRoomId(1L));
    }

    @Test
    void processMessage() {
        when(modelMapper.map(expectedChatMessageDto, ChatMessage.class)).thenReturn(expectedChatMessage);
        when(chatMessageRepo.save(expectedChatMessage)).thenReturn(expectedChatMessage);
        chatMessageServiceImpl.processMessage(expectedChatMessageDto);
        verify(messagingTemplate).convertAndSend("/room/" + expectedChatMessageDto.getRoomId() + "/queue/messages",
            expectedChatMessageDto);
    }

    @Test
    public void deleteMessage() {
        when(modelMapper.map(expectedChatMessageDto, ChatMessage.class)).thenReturn(expectedChatMessage);

        chatMessageServiceImpl.deleteMessage(expectedChatMessageDto);

        verify(chatMessageRepo).delete(expectedChatMessage);
        verify(messagingTemplate).convertAndSend(any(String.class),
            any(ChatMessageDto.class), any(Map.class));
    }

    @Test
    public void updateMessage() {
        expectedChatMessageDto.setId(1L);
        when(modelMapper.map(expectedChatMessageDto, ChatMessage.class)).thenReturn(expectedChatMessage);
        when(chatMessageRepo.getLikesByMessageId(anyLong()))
            .thenReturn(Collections.singletonList(expectedChatMessageDto.getId()));
        when(chatMessageRepo.save(expectedChatMessage)).thenReturn(expectedChatMessage);

        chatMessageServiceImpl.updateMessage(expectedChatMessageDto);

        verify(messagingTemplate).convertAndSend(any(String.class),
            any(ChatMessageDto.class), any(Map.class));
    }

    @Test
    public void findTopByOrderByIdDesc() {
        when(chatMessageRepo.findTopByOrderByIdDesc()).thenReturn(expectedChatMessage);
        when(modelMapper.map(expectedChatMessage, ChatMessageDto.class)).thenReturn(expectedChatMessageDto);

        ChatMessageDto actual = chatMessageServiceImpl.findTopByOrderByIdDesc();

        assertEquals(expectedChatMessageDto, actual);
    }

    @Test
    public void likeMessage() {
        MessageLike liked = new MessageLike(1L, 1L);

        when(chatMessageRepo.getParticipantIdIfLiked(liked.getMessageId(), liked.getParticipantId())).thenReturn(1L);
        when(chatMessageRepo.findById(liked.getMessageId())).thenReturn(Optional.of(expectedChatMessage));
        when(modelMapper.map(any(ChatMessage.class), eq(ChatMessageDto.class))).thenReturn(expectedChatMessageDto);

        chatMessageServiceImpl.likeMessage(liked);

        verify(chatMessageRepo).getLikesByMessageId(liked.getMessageId());
        verify(chatMessageRepo).deleteLikeFromMessage(liked.getMessageId(), liked.getParticipantId());
        verify(messagingTemplate).convertAndSend(any(String.class),
            any(ChatMessageDto.class), any(Map.class));
    }

    @Test
    public void likeMessageUnliked() {
        Long id = null;
        MessageLike liked = new MessageLike(1L, 1L);

        when(chatMessageRepo.getParticipantIdIfLiked(anyLong(), anyLong())).thenReturn(id);
        when(chatMessageRepo.findById(liked.getMessageId())).thenReturn(Optional.of(expectedChatMessage));
        when(modelMapper.map(any(ChatMessage.class), eq(ChatMessageDto.class))).thenReturn(expectedChatMessageDto);

        chatMessageServiceImpl.likeMessage(liked);

        verify(chatMessageRepo).addLikeToMessage(liked.getMessageId(), liked.getParticipantId());
        verify(chatMessageRepo).getLikesByMessageId(liked.getMessageId());
        verify(messagingTemplate).convertAndSend(any(String.class),
            any(ChatMessageDto.class), any(Map.class));
    }
}
