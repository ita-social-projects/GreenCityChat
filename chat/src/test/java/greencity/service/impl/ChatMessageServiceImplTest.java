package greencity.service.impl;

import greencity.dto.ChatMessageDto;
import greencity.dto.ChatMessageResponseDto;
import greencity.dto.MessageLike;
import greencity.dto.PageableDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.SortOrder;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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

    ChatMessageDto chatMessageDto;
    ChatMessage expectedChatMessage;
    ChatMessageResponseDto responseDto;
    Set<Participant> participants;

    @BeforeEach
    void init() {
        expectedChatMessageDto = ChatMessageDto.builder()
            .roomId(1L)
            .senderId(1L)
            .createDate(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()))
            .content("test").senderId(1L)
            .build();
        expectedChatMessage = ChatMessage.builder()
            .id(1L)
            .content("Content")
            .createDate(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()))
            .sender(Participant.builder()
                .id(1L)
                .name("User").build())
            .build();
        responseDto = ChatMessageResponseDto.builder()
            .id(1L)
            .content("Content")
            .createDate(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()).toString())
            .senderId(1L)
            .roomId(1L)
            .build();
        chatMessageDto = ChatMessageDto.builder()
            .roomId(1L)
            .senderId(1L)
            .createDate(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()))
            .content("test").senderId(1L)
            .build();
        participants = Set.of(Participant.builder()
            .id(1L)
            .name("User").build());
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
        PageRequest pageRequest =
            PageRequest.of(0, 1, Sort.by(Sort.Direction.valueOf(SortOrder.DESC.toString()), "createDate"));
        Page<ChatMessage> messages = new PageImpl<>(Collections.singletonList(chatMessage), pageRequest, 1);
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
            .id(1L)
            .content("test")
            .roomId(1L)
            .senderId(1L)
            .build();
        List<ChatMessageDto> chatMessageDtos = Collections.singletonList(chatMessageDto);
        PageableDto pageableDto = new PageableDto<>(
            chatMessageDtos,
            messages.getTotalElements(),
            messages.getPageable().getPageNumber(),
            messages.getTotalPages());

        when(chatRoomRepo.findById(1L)).thenReturn(roomOptional);

        when(chatMessageRepo.findAllByRoom(chatRoom, pageRequest)).thenReturn(messages);

        when(modelMapper.map(messages.getContent().get(0), ChatMessageDto.class)).thenReturn(chatMessageDto);

        PageableDto<ChatMessageDto> actual = chatMessageServiceImpl.findAllMessagesByChatRoomId(1L, pageRequest);
        assertEquals(pageableDto, actual);
    }

    @Test
    void processMessage() {
        when(modelMapper.map(expectedChatMessageDto, ChatMessage.class)).thenReturn(expectedChatMessage);

        when(chatMessageRepo.save(expectedChatMessage)).thenReturn(null);

        when(modelMapper.map(null, ChatMessageDto.class)).thenReturn(chatMessageDto);

        when(chatRoomRepo.getPatricipantsByChatRoomId(anyLong())).thenReturn(participants);

        when(modelMapper.map(chatMessageDto, ChatMessageResponseDto.class)).thenReturn(responseDto);

        chatMessageServiceImpl.processMessage(expectedChatMessageDto);

        verify(messagingTemplate).convertAndSend("/room/" + "/message/chat-messages" + 1L,
            responseDto);
    }

    @Test
    void deleteMessage() {
        when(modelMapper.map(chatMessageDto, ChatMessage.class)).thenReturn(expectedChatMessage);
        doNothing().when(chatMessageRepo).delete(expectedChatMessage);
        doNothing().when(messagingTemplate).convertAndSend(eq("/room/1/queue/messages"), eq(chatMessageDto),
                anyMap());
        chatMessageServiceImpl.deleteMessage(expectedChatMessageDto);

        verify(chatMessageRepo).delete(expectedChatMessage);
    }

}
