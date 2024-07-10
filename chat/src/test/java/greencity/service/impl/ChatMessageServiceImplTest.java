package greencity.service.impl;

import greencity.dto.ChatMessageDto;
import greencity.dto.ChatMessageResponseDto;
import greencity.dto.PageableDto;
import greencity.dto.MessageLike;
import greencity.dto.ChatFileDto;
import greencity.dto.ChatMessageWithFileDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.FilesType;
import greencity.enums.SortOrder;
import greencity.exception.exceptions.ChangesNotSavedException;
import greencity.exception.exceptions.ChatRoomNotFoundException;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;

import java.lang.reflect.Method;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.Map;

import greencity.service.AzureFileService;
import greencity.service.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import org.webjars.NotFoundException;

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
    @Mock
    private AzureFileService azureFileService;
    @Mock
    private Principal principal;
    @Mock
    private ParticipantService participantService;
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
    void findAllMessagesByChatRoomIdTest() {
        String email = "user@example.com";
        Participant owner = Participant.builder()
            .id(1L)
            .email(email)
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
            PageRequest.of(0, 1, Sort.by(Sort.Direction.valueOf(SortOrder.DESC.toString()),
                "createDate"));
        Page<ChatMessage> messages = new PageImpl<>(Collections.singletonList(chatMessage), pageRequest, 1);
        ChatMessageWithFileDto chatMessageWithFileDto = ChatMessageWithFileDto.builder()
            .id(1L)
            .content("test")
            .roomId(1L)
            .senderId(1L)
            .unread(false)
            .build();
        List<ChatMessageWithFileDto> chatMessageDtos = Collections.singletonList(chatMessageWithFileDto);
        PageableDto pageableDto = new PageableDto<>(
            chatMessageDtos,
            messages.getTotalElements(),
            messages.getPageable().getPageNumber(),
            messages.getTotalPages());

        when(chatRoomRepo.findById(1L)).thenReturn(roomOptional);

        when(chatMessageRepo.findAllByRoom(chatRoom, pageRequest)).thenReturn(messages);

        when(modelMapper.map(messages.getContent().get(0), ChatMessageWithFileDto.class))
            .thenReturn(chatMessageWithFileDto);
        when(principal.getName()).thenReturn(email);
        when(participantService.findByEmail(email)).thenReturn(owner);

        PageableDto<ChatMessageWithFileDto> actual =
            chatMessageServiceImpl.findAllMessagesByChatRoomId(1L, pageRequest, principal);
        assertEquals(pageableDto, actual);
    }

    @Test
    void findAllMessagesByChatRoomIdTest_WhenChatRoomDoesNotExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(chatRoomRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ChatRoomNotFoundException.class, () -> {
            chatMessageServiceImpl.findAllMessagesByChatRoomId(1L, pageable, principal);
        });
    }

    @Test
    void processMessage() {
        when(modelMapper.map(expectedChatMessageDto, ChatMessage.class)).thenReturn(expectedChatMessage);

        when(chatMessageRepo.save(expectedChatMessage)).thenReturn(null);

        when(modelMapper.map(null, ChatMessageDto.class)).thenReturn(chatMessageDto);

        when(chatRoomRepo.getPatricipantsByChatRoomId(anyLong())).thenReturn(participants);

        when(modelMapper.map(chatMessageDto, ChatMessageResponseDto.class)).thenReturn(responseDto);

        chatMessageServiceImpl.processMessage(expectedChatMessageDto);

        verify(messagingTemplate).convertAndSend("/room" + "/message/chat-messages" + 1L,
            responseDto);
    }

    @Test
    void deleteLikeFromMessage() {
        MessageLike messageLike = new MessageLike(1L, 1L);
        when(chatMessageRepo.getParticipantIdIfLiked(1L, 1L)).thenReturn(1L);
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .content("Content")
            .createDate(ZonedDateTime.now())
            .sender(Participant.builder()
                .id(1L)
                .name("User").build())
            .room(ChatRoom.builder().id(1L).build())
            .build();
        when(chatMessageRepo.findById(1L))
            .thenReturn(Optional.of(chatMessage));
        when(modelMapper.map(chatMessage, ChatMessageWithFileDto.class))
            .thenReturn(new ChatMessageWithFileDto());
        chatMessageServiceImpl.likeMessage(messageLike);
        verify(chatMessageRepo).deleteLikeFromMessage(1L, 1L);
    }

    @Test
    void likeMessage() {
        MessageLike messageLike = new MessageLike(1L, 1L);
        when(chatMessageRepo.getParticipantIdIfLiked(1L, 1L)).thenReturn(null);
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .content("Content")
            .createDate(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()))
            .sender(Participant.builder()
                .id(1L)
                .name("User").build())
            .room(ChatRoom.builder().id(1L).build())
            .build();
        when(chatMessageRepo.findById(1L))
            .thenReturn(Optional.of(chatMessage));
        when(modelMapper.map(chatMessage, ChatMessageWithFileDto.class))
            .thenReturn(new ChatMessageWithFileDto());
        chatMessageServiceImpl.likeMessage(messageLike);
        verify(chatMessageRepo).addLikeToMessage(1L, 1L);
    }

    @Test
    void likeMessageNotFound() {
        MessageLike messageLike = new MessageLike(1L, 1L);

        when(chatMessageRepo.getParticipantIdIfLiked(1L, 1L)).thenReturn(null);
        doNothing().when(chatMessageRepo).addLikeToMessage(1L, 1L);
        when(chatMessageRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> chatMessageServiceImpl.likeMessage(messageLike));
        verify(chatMessageRepo, never()).deleteLikeFromMessage(anyLong(), anyLong());
    }

    @Test
    void sendVoiceMessageTest() {
        ChatMessageDto inputDto = chatMessageDto;
        ChatMessage expectedMessage = expectedChatMessage;
        MultipartFile mockVoiceFile = mock(MultipartFile.class);
        ChatFileDto mockedFileDto = new ChatFileDto("testFile.mp3", FilesType.AUDIO,
            "https://example.com/testFile.mp3");
        ChatMessageWithFileDto expectedDto = ChatMessageWithFileDto.builder()
            .roomId(1L)
            .senderId(1L)
            .createDate(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()))
            .content("test").senderId(1L)
            .fileName(mockedFileDto.getFileName())
            .fileType(mockedFileDto.getFileType().toString())
            .fileUrl(mockedFileDto.getFileUrl())
            .build();
        expectedMessage.setFileUrl(mockedFileDto.getFileUrl());
        expectedMessage.setFileName(mockedFileDto.getFileName());
        expectedMessage.setFileType(mockedFileDto.getFileType().toString());

        when(azureFileService.saveVoiceMessage(any(MultipartFile.class))).thenReturn(mockedFileDto);
        when(modelMapper.map(expectedDto, ChatMessage.class))
            .thenReturn(expectedMessage);
        when(chatMessageRepo.save(any(ChatMessage.class))).thenReturn(expectedMessage);
        when(modelMapper.map(expectedMessage, ChatMessageWithFileDto.class))
            .thenReturn(expectedDto);

        ChatMessageWithFileDto resultDto = chatMessageServiceImpl.sendVoiceMessage(inputDto, mockVoiceFile);

        verify(azureFileService).saveVoiceMessage(any(MultipartFile.class));
        verify(chatMessageRepo).save(any(ChatMessage.class));

        assertEquals(resultDto, expectedDto);
    }

    @Test
    void sendFileTest() {
        MultipartFile mockFile = mock(MultipartFile.class);
        ChatMessageDto inputDto = chatMessageDto;
        ChatMessage expectedMessage = expectedChatMessage;
        FilesType fileType = FilesType.FILE;
        ChatFileDto mockedFileDto = new ChatFileDto("testFile.pdf", fileType,
            "https://example.com/testFile.pdf");
        ChatMessageWithFileDto expectedDto = ChatMessageWithFileDto.builder()
            .roomId(1L)
            .senderId(1L)
            .createDate(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()))
            .content("test").senderId(1L)
            .fileName(mockedFileDto.getFileName())
            .fileType(mockedFileDto.getFileType().toString())
            .fileUrl(mockedFileDto.getFileUrl())
            .build();
        expectedMessage.setFileUrl(mockedFileDto.getFileUrl());
        expectedMessage.setFileName(mockedFileDto.getFileName());
        expectedMessage.setFileType(mockedFileDto.getFileType().toString());

        when(azureFileService.saveFile(any(MultipartFile.class), any())).thenReturn(mockedFileDto);
        when(modelMapper.map(expectedDto, ChatMessage.class))
            .thenReturn(expectedMessage);
        when(chatMessageRepo.save(any(ChatMessage.class))).thenReturn(expectedMessage);
        when(modelMapper.map(expectedMessage, ChatMessageWithFileDto.class))
            .thenReturn(expectedDto);

        ChatMessageWithFileDto resultDto = chatMessageServiceImpl.sendFile(inputDto, mockFile, fileType);

        verify(azureFileService).saveFile(any(MultipartFile.class), eq(fileType));
        verify(chatMessageRepo).save(any(ChatMessage.class));

        assertEquals(resultDto, expectedDto);
    }

    @Test
    void mergeChatMessageAndFileTest() throws Exception {
        ChatFileDto chatFileDto = new ChatFileDto("testFile.mp3", FilesType.AUDIO,
            "https://example.com/testFile.mp3");
        ChatMessageDto dto = ChatMessageDto.builder()
            .id(1L)
            .roomId(2L)
            .senderId(3L)
            .content("Test content")
            .createDate(ZonedDateTime.now())
            .build();

        Method method = ChatMessageServiceImpl.class.getDeclaredMethod("mergeChatMessageAndFile",
            ChatMessageDto.class, ChatFileDto.class);
        method.setAccessible(true);

        ChatMessageWithFileDto result = (ChatMessageWithFileDto) method.invoke(chatMessageServiceImpl,
            dto, chatFileDto);

        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getRoomId(), result.getRoomId());
        assertEquals(dto.getSenderId(), result.getSenderId());
        assertEquals(dto.getContent(), result.getContent());
        assertEquals(dto.getCreateDate(), result.getCreateDate());
        assertEquals(chatFileDto.getFileName(), result.getFileName());
        assertEquals(chatFileDto.getFileType().toString(), result.getFileType());
        assertEquals(chatFileDto.getFileUrl(), result.getFileUrl());
    }

    @Test
    void deleteMessageTest_successfulWithoutFile() {
        ChatMessageWithFileDto chatMessageWithFileDto = ChatMessageWithFileDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Content")
            .build();
        ChatMessageDto messageDto = ChatMessageDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Content")
            .build();
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .room(ChatRoom.builder().id(1L).name("TestName").build())
            .sender(Participant.builder().id(1L).name("User").build())
            .content("Content")
            .build();

        when(chatMessageRepo.findById(anyLong())).thenReturn(Optional.of(chatMessage));
        doNothing().when(chatMessageRepo).delete(chatMessage);
        when(modelMapper.map(chatMessage, ChatMessageWithFileDto.class)).thenReturn(chatMessageWithFileDto);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());

        chatMessageServiceImpl.deleteMessage(messageDto);

        verify(chatMessageRepo).delete(chatMessage);
        verify(azureFileService, never()).deleteFile(anyString());
        verify(messagingTemplate).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());
    }

    @Test
    void deleteMessageTest_successfulWithFile() {
        ChatMessageWithFileDto chatMessageWithFileDto = ChatMessageWithFileDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Content")
            .build();
        ChatMessageDto messageDto = ChatMessageDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Content")
            .build();
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .room(ChatRoom.builder().id(1L).name("TestName").build())
            .sender(Participant.builder().id(1L).name("User").build())
            .content("Content")
            .fileName("testfile.txt")
            .build();

        when(chatMessageRepo.findById(anyLong())).thenReturn(Optional.of(chatMessage));
        doNothing().when(chatMessageRepo).delete(chatMessage);
        when(modelMapper.map(chatMessage, ChatMessageWithFileDto.class)).thenReturn(chatMessageWithFileDto);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());
        doNothing().when(azureFileService).deleteFile("testfile.txt");

        chatMessageServiceImpl.deleteMessage(messageDto);

        verify(chatMessageRepo).delete(chatMessage);
        verify(azureFileService).deleteFile("testfile.txt");
        verify(messagingTemplate).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());
    }

    @Test
    void deleteMessageTest_messageNotFound() {
        ChatMessageDto messageDto = ChatMessageDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Content")
            .build();

        when(chatMessageRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> chatMessageServiceImpl.deleteMessage(messageDto));

        verify(chatMessageRepo, never()).delete(any(ChatMessage.class));
        verify(azureFileService, never()).deleteFile(anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());
    }

    @Test
    void updateMessageTest_successful() {
        ChatMessageWithFileDto chatMessageWithFileDto = ChatMessageWithFileDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Updated Content")
            .build();
        ChatMessageDto messageDto = ChatMessageDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Updated Content")
            .build();
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .room(ChatRoom.builder().id(1L).name("TestName").build())
            .sender(Participant.builder().id(1L).name("User").build())
            .content("Content")
            .build();

        when(chatMessageRepo.findById(anyLong())).thenReturn(Optional.of(chatMessage));
        when(chatMessageRepo.save(chatMessage)).thenReturn(chatMessage);
        when(modelMapper.map(chatMessage, ChatMessageWithFileDto.class)).thenReturn(chatMessageWithFileDto);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());

        chatMessageServiceImpl.updateMessage(messageDto);

        verify(chatMessageRepo).save(chatMessage);
        verify(messagingTemplate).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());
    }

    @Test
    void updateMessageTest_messageNotFound() {
        ChatMessageDto messageDto = ChatMessageDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("Updated Content")
            .build();

        when(chatMessageRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> chatMessageServiceImpl.updateMessage(messageDto));

        verify(chatMessageRepo, never()).save(any(ChatMessage.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());
    }

    @Test
    void updateMessageTest_emptyContent() {
        ChatMessageDto messageDto = ChatMessageDto.builder()
            .id(1L)
            .roomId(1L)
            .senderId(1L)
            .content("")
            .build();
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .room(ChatRoom.builder().id(1L).name("TestName").build())
            .sender(Participant.builder().id(1L).name("User").build())
            .content("Content")
            .build();

        when(chatMessageRepo.findById(anyLong())).thenReturn(Optional.of(chatMessage));

        assertThrows(ChangesNotSavedException.class, () -> chatMessageServiceImpl.updateMessage(messageDto));

        verify(chatMessageRepo, never()).save(any(ChatMessage.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(ChatMessageWithFileDto.class),
            (Map<String, Object>) any());
    }
}
