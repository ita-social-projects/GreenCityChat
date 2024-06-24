package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.*;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.service.AzureFileService;
import greencity.service.ChatMessageService;
import greencity.service.ChatRoomService;
import greencity.service.ParticipantService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatControllerTest {
    private static final String chatLink = "/chat";

    private MockMvc mockMvc;

    @InjectMocks
    private ChatController chatController;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private AzureFileService azureFileService;

    @Mock
    private Principal principal;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(chatController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    }

    @Test
    void findAllRoomsTest() throws Exception {
        when(principal.getName()).thenReturn("test");
        List<ChatRoomDto> list = new ArrayList<>();
        when(chatRoomService.findAllByParticipantName("test")).thenReturn(list);
        mockMvc.perform(get(chatLink + "/").principal(principal)).andExpect(status().isOk());

        verify(chatRoomService).findAllByParticipantName("test");

    }

    @Test
    void findAllVisibleRoomsTest() throws Exception {
        when(principal.getName()).thenReturn("test");
        List<ChatRoomDto> list = new ArrayList<>();
        when(chatRoomService.findAllVisibleRooms("test")).thenReturn(list);
        mockMvc.perform(get(chatLink + "/rooms/visible").principal(principal)).andExpect(status().isOk());

        verify(chatRoomService).findAllVisibleRooms("test");
    }

    @Test
    void findAllMessagesTest() throws Exception {
        mockMvc.perform(get(chatLink + "/messages/{room_id}", 1))
            .andExpect(status().isOk());

        verify(chatMessageService).findAllMessagesByChatRoomId(1L, PageRequest.of(0, 20));
    }

    @Test
    void findPrivateRoomWithUserTest() throws Exception {
        when(principal.getName()).thenReturn("name");
        mockMvc.perform(get(chatLink + "/user/{id}", 1)
            .principal(principal))
            .andExpect(status().isOk());

        verify(chatRoomService).findPrivateByParticipants(1L, "name");
    }

    @Test
    void findRoomByIdTest() throws Exception {
        mockMvc.perform(get(chatLink + "/room/{room_id}", 1))
            .andExpect(status().isOk());
        verify(chatRoomService).findChatRoomById(1L);
    }

    @Test
    void cleanUnreadMessagesTest() throws Exception {
        mockMvc.perform(delete(chatLink + "/room/{user_id}/{room_id}", 1, 1))
            .andExpect(status().isOk());

        verify(chatMessageService).cleanUnreadMessages(1L, 1L);
    }

    @Test
    void getCurrentUserTest() throws Exception {
        when(principal.getName()).thenReturn("testmail@gmail.com");
        mockMvc.perform(get(chatLink + "/user")
            .principal(principal)).andExpect(status().isOk());

        verify(participantService).getCurrentParticipantByEmail("testmail@gmail.com");
    }

    @Test
    void getAllParticipantsByTest() throws Exception {
        String query = "testQuery";
        when(principal.getName()).thenReturn("testmail@gmail.com");
        List<ParticipantDto> listOfAllParticipantsByQuery = new ArrayList<>();
        when(participantService.findAllParticipantsByQuery(query, "testmail@gmail.com"))
            .thenReturn(listOfAllParticipantsByQuery);

        mockMvc.perform(get(chatLink + "/users/{query}", query)
            .principal(principal)).andExpect(status().isOk());

        verify(participantService).findAllParticipantsByQuery(query, "testmail@gmail.com");
    }

    @Test
    void getAllParticipantsByWithEmptyPrincipalTest() throws Exception {
        List<ParticipantDto> list =
            List.of(ParticipantDto.builder().id(1L).build(), ParticipantDto.builder().id(2L).build());
        when(principal.getName()).thenReturn("testmail@gmail.com");
        when(participantService.findAllExceptCurrentUser("testmail@gmail.com")).thenReturn(list);

        mockMvc.perform(get(chatLink + "/users/{query}", "")
            .principal(principal)).andExpect(status().isOk());

        verify(participantService).findAllExceptCurrentUser("testmail@gmail.com");
    }

    @Test
    void getAllChatRoomsByTest() throws Exception {
        String query = "testQuery";
        when(principal.getName()).thenReturn("testmail@gmail.com");
        Participant participant = new Participant();
        when(participantService.findByEmail("testmail@gmail.com")).thenReturn(participant);
        List<ChatRoomDto> listOfAllChatRoomsByQuery = new ArrayList<>();
        when(chatRoomService.findAllChatRoomsByQuery(query, participant)).thenReturn(listOfAllChatRoomsByQuery);

        mockMvc.perform(get(chatLink + "/rooms/{query}", query)
            .principal(principal)).andExpect(status().isOk());

        verify(chatRoomService).findAllChatRoomsByQuery(query, participant);
    }

    @Test
    void getAllChatRoomsByWithEmptyQueryTest() throws Exception {
        when(principal.getName()).thenReturn("test");
        List<ChatRoomDto> list = List.of(ChatRoomDto.builder().id(1L).build());
        when(chatRoomService.findAllVisibleRooms("test")).thenReturn(list);

        mockMvc.perform(get(chatLink + "/rooms").principal(principal)).andExpect(status().isOk());

        verify(chatRoomService).findAllVisibleRooms("test");
    }

    @Test
    void getLastIdTest() throws Exception {
        ChatMessageDto chatMessage = new ChatMessageDto();
        chatMessage.setId(1L);
        when(chatMessageService.findTopByOrderByIdDesc()).thenReturn(chatMessage);
        mockMvc.perform(get(chatLink + "/last/message"))
            .andExpect(status().isOk());

        verify(chatMessageService).findTopByOrderByIdDesc();
    }

    @Test
    void sentMessageTest() throws Exception {
        Long userId = 1L, roomId = 1L;
        String content = "content";
        when(chatMessageService.sentMessage(Mockito.eq(userId), Mockito.eq(roomId), Mockito.eq(content)))
            .thenReturn(ChatMessageDto.builder()
                .id(1L).build());
        mockMvc.perform(post(chatLink + "/sent-message/{userId}/{roomId}", userId, roomId)
            .param("content", content)).andExpect(status().isCreated());

        verify(chatMessageService).sentMessage(userId, roomId, content);
    }

    @Test
    void createChatRoomTest() throws Exception {
        GroupChatRoomCreateDto chatRoomCreateDto = GroupChatRoomCreateDto.builder()
            .chatName("testName")
            .build();
        ChatRoomDto chatRoomDto = ChatRoomDto.builder().name("testName").build();
        ObjectMapper objectMapper = new ObjectMapper();

        when(chatRoomService.createNewChatRoom(Mockito.eq(chatRoomCreateDto))).thenReturn(chatRoomDto);
        mockMvc.perform(post(chatLink + "/create-chatRoom")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(chatRoomCreateDto))).andExpect(status().isCreated()).andReturn();

        verify(chatRoomService).createNewChatRoom(Mockito.eq(chatRoomCreateDto));
    }

    @Test
    void chatExistTest() throws Exception {
        Long fistUserId = 1L, secondUserId = 2L;
        when(chatMessageService.chatExist(Mockito.eq(fistUserId), Mockito.eq(secondUserId))).thenReturn(
            FriendsChatDto.builder()
                .chatId(1L).build());
        mockMvc.perform(get(chatLink + "/exist/{fistUserId}/{secondUserId}", fistUserId, secondUserId))
            .andExpect(status().isOk());

        verify(chatMessageService).chatExist(fistUserId, secondUserId);
    }

    @Test
    void getGroupChatsTest() throws Exception {
        ChatType chatType = ChatType.GROUP;
        when(principal.getName()).thenReturn("testmail@gmail.com");
        Participant participant = new Participant();
        when(participantService.findByEmail("testmail@gmail.com")).thenReturn(participant);
        List<ChatRoomDto> listOfGroupChatRooms = new ArrayList<>();
        when(chatRoomService.findGroupChatRooms(participant, chatType)).thenReturn(listOfGroupChatRooms);
        mockMvc.perform(get(chatLink + "/groups").principal(principal))
            .andExpect(status().isOk());

        verify(chatRoomService).findGroupChatRooms(participant, chatType);
    }

    @Test
    void uploadImageTest() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "testImage.png",
            "image/png", new byte[1]);
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
            .senderId(1L)
            .roomId(1L)
            .content("content")
            .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String chatMessageDtoJson = objectMapper.writeValueAsString(chatMessageDto);
        MockMultipartFile chatMessageDtoFile = new MockMultipartFile("chatMessageDto", "",
            "application/json", chatMessageDtoJson.getBytes());

        when(chatMessageService.sendFile(any(ChatMessageDto.class), any(MultipartFile.class), any(String.class)))
            .thenReturn(chatMessageDto);

        mockMvc.perform(multipart(chatLink +"/upload/image")
            .file(image)
            .file(chatMessageDtoFile)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());

        verify(chatMessageService).sendFile(any(ChatMessageDto.class), any(MultipartFile.class), any(String.class));
    }

    @Test
    void uploadFileTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "testFile.pdf",
            "application/pdf", new byte[1]);
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
            .senderId(1L)
            .roomId(1L)
            .content("content")
            .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String chatMessageDtoJson = objectMapper.writeValueAsString(chatMessageDto);
        MockMultipartFile chatMessageDtoFile = new MockMultipartFile("chatMessageDto", "",
            "application/json", chatMessageDtoJson.getBytes());

        when(chatMessageService.sendFile(any(ChatMessageDto.class), any(MultipartFile.class), any(String.class)))
            .thenReturn(chatMessageDto);

        mockMvc.perform(multipart(chatLink + "/upload/file")
            .file(file)
            .file(chatMessageDtoFile)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());

        verify(chatMessageService).sendFile(any(ChatMessageDto.class), any(MultipartFile.class), any(String.class));
    }

    @Test
    void uploadVoiceTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "testFile.mp3",
            "audio/mp3", new byte[1]);
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
            .senderId(1L)
            .roomId(1L)
            .content("content")
            .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String chatMessageDtoJson = objectMapper.writeValueAsString(chatMessageDto);
        MockMultipartFile chatMessageDtoFile = new MockMultipartFile("chatMessageDto", "",
            "application/json", chatMessageDtoJson.getBytes());

        when(chatMessageService.sendVoiceMessage(any(ChatMessageDto.class), any(MultipartFile.class)))
            .thenReturn(chatMessageDto);

        mockMvc.perform(multipart(chatLink + "/upload/voice")
            .file(file)
            .file(chatMessageDtoFile)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());

        verify(chatMessageService).sendVoiceMessage(any(ChatMessageDto.class), any(MultipartFile.class));
}

    @Test
    void deleteFileTest() throws Exception {
        mockMvc.perform(delete(chatLink + "/delete/file/{fileName}", "fileName"))
            .andExpect(status().isOk());

        verify(this.azureFileService).deleteFile("fileName");
    }

    @Test
    void addUserToSystemChatRoomTest() throws Exception {
        Long userId = 1L;
        when(chatRoomService.addNewUserToSystemChat(userId)).thenReturn(userId);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post(chatLink + "/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isOk());

        verify(chatRoomService).addNewUserToSystemChat(userId);
    }

    @Test
    @SneakyThrows
    void deleteAllMessagesFromChatRoomTest() {
        mockMvc.perform(delete(chatLink + "/room/378/10/delete"))
            .andExpect(status().isAccepted());
    }

    @Test
    void processMessageTest() {
        ChatMessageDto chatMessageDto = ChatMessageDto.builder().id(1L).build();
        chatController.processMessage(chatMessageDto);
        verify(chatMessageService).processMessage(chatMessageDto);
    }

    @Test
    void deleteMessageTest() {
        ChatMessageDto chatMessageDto = ChatMessageDto.builder().id(1L).build();
        chatController.deleteMessage(chatMessageDto);
        verify(chatMessageService).deleteMessage(chatMessageDto);
    }

    @Test
    void updateMessageTest() {
        ChatMessageDto chatMessageDto = ChatMessageDto.builder().id(1L).build();
        chatController.updateMessage(chatMessageDto);
        verify(chatMessageService).updateMessage(chatMessageDto);
    }

    @Test
    void likeMessageTest() {
        MessageLike messageLike = new MessageLike(1L, 1L);
        chatController.likeMessage(messageLike);
        verify(chatMessageService).likeMessage(messageLike);
    }
}
