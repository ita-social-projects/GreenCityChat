package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.ChatMessageDto;
import greencity.dto.ChatRoomDto;
import greencity.dto.FriendsChatDto;
import greencity.dto.GroupChatRoomCreateDto;
import greencity.dto.MessageLike;
import greencity.dto.ParticipantDto;
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
        Mockito.when(principal.getName()).thenReturn("test");
        List<ChatRoomDto> list = new ArrayList<>();
        Mockito.when(chatRoomService.findAllByParticipantName("test")).thenReturn(list);
        mockMvc.perform(get(chatLink + "/").principal(principal)).andExpect(status().isOk());

        Mockito.verify(chatRoomService).findAllByParticipantName("test");

    }

    @Test
    void findAllVisibleRoomsTest() throws Exception {
        Mockito.when(principal.getName()).thenReturn("test");
        List<ChatRoomDto> list = new ArrayList<>();
        Mockito.when(chatRoomService.findAllVisibleRooms("test")).thenReturn(list);
        mockMvc.perform(get(chatLink + "/rooms/visible").principal(principal)).andExpect(status().isOk());

        Mockito.verify(chatRoomService).findAllVisibleRooms("test");
    }

    @Test
    void findAllMessagesTest() throws Exception {
        mockMvc.perform(get(chatLink + "/messages/{room_id}", 1))
            .andExpect(status().isOk());

        Mockito.verify(chatMessageService).findAllMessagesByChatRoomId(1L, PageRequest.of(0, 20));
    }

    @Test
    void findPrivateRoomWithUserTest() throws Exception {
        Mockito.when(principal.getName()).thenReturn("name");
        mockMvc.perform(get(chatLink + "/user/{id}", 1)
            .principal(principal))
            .andExpect(status().isOk());

        Mockito.verify(chatRoomService).findPrivateByParticipants(1L, "name");
    }

    @Test
    void findRoomByIdTest() throws Exception {
        mockMvc.perform(get(chatLink + "/room/{room_id}", 1))
            .andExpect(status().isOk());
        Mockito.verify(chatRoomService).findChatRoomById(1L);
    }

    @Test
    void cleanUnreadMessagesTest() throws Exception {
        mockMvc.perform(delete(chatLink + "/room/{user_id}/{room_id}", 1, 1))
            .andExpect(status().isOk());

        Mockito.verify(chatMessageService).cleanUnreadMessages(1L, 1L);
    }

    @Test
    void getCurrentUserTest() throws Exception {
        Mockito.when(principal.getName()).thenReturn("testmail@gmail.com");
        mockMvc.perform(get(chatLink + "/user")
            .principal(principal)).andExpect(status().isOk());

        Mockito.verify(participantService).getCurrentParticipantByEmail("testmail@gmail.com");
    }

    @Test
    void getAllParticipantsByTest() throws Exception {
        String query = "testQuery";
        Mockito.when(principal.getName()).thenReturn("testmail@gmail.com");
        List<ParticipantDto> listOfAllParticipantsByQuery = new ArrayList<>();
        Mockito.when(participantService.findAllParticipantsByQuery(query, "testmail@gmail.com"))
            .thenReturn(listOfAllParticipantsByQuery);

        mockMvc.perform(get(chatLink + "/users/{query}", query)
            .principal(principal)).andExpect(status().isOk());

        Mockito.verify(participantService).findAllParticipantsByQuery(query, "testmail@gmail.com");
    }

    @Test
    void getAllParticipantsByWithEmptyPrincipalTest() throws Exception {
        List<ParticipantDto> list =
            List.of(ParticipantDto.builder().id(1L).build(), ParticipantDto.builder().id(2L).build());
        Mockito.when(principal.getName()).thenReturn("testmail@gmail.com");
        Mockito.when(participantService.findAllExceptCurrentUser("testmail@gmail.com")).thenReturn(list);

        mockMvc.perform(get(chatLink + "/users/{query}", "")
            .principal(principal)).andExpect(status().isOk());

        Mockito.verify(participantService).findAllExceptCurrentUser("testmail@gmail.com");
    }

    @Test
    void getAllChatRoomsByTest() throws Exception {
        String query = "testQuery";
        Mockito.when(principal.getName()).thenReturn("testmail@gmail.com");
        Participant participant = new Participant();
        Mockito.when(participantService.findByEmail("testmail@gmail.com")).thenReturn(participant);
        List<ChatRoomDto> listOfAllChatRoomsByQuery = new ArrayList<>();
        Mockito.when(chatRoomService.findAllChatRoomsByQuery(query, participant)).thenReturn(listOfAllChatRoomsByQuery);

        mockMvc.perform(get(chatLink + "/rooms/{query}", query)
            .principal(principal)).andExpect(status().isOk());

        Mockito.verify(chatRoomService).findAllChatRoomsByQuery(query, participant);
    }

    @Test
    void getAllChatRoomsByWithEmptyQueryTest() throws Exception {
        Mockito.when(principal.getName()).thenReturn("test");
        List<ChatRoomDto> list = List.of(ChatRoomDto.builder().id(1L).build());
        Mockito.when(chatRoomService.findAllVisibleRooms("test")).thenReturn(list);

        mockMvc.perform(get(chatLink + "/rooms").principal(principal)).andExpect(status().isOk());

        Mockito.verify(chatRoomService).findAllVisibleRooms("test");
    }

    @Test
    void getLastIdTest() throws Exception {
        ChatMessageDto chatMessage = new ChatMessageDto();
        chatMessage.setId(1L);
        Mockito.when(chatMessageService.findTopByOrderByIdDesc()).thenReturn(chatMessage);
        mockMvc.perform(get(chatLink + "/last/message"))
            .andExpect(status().isOk());

        Mockito.verify(chatMessageService).findTopByOrderByIdDesc();
    }

    @Test
    void sentMessageTest() throws Exception {
        Long userId = 1L, roomId = 1L;
        String content = "content";
        Mockito.when(chatMessageService.sentMessage(Mockito.eq(userId), Mockito.eq(roomId), Mockito.eq(content))).thenReturn(ChatMessageDto.builder()
            .id(1L).build());
        mockMvc.perform(post(chatLink + "/sent-message/{userId}/{roomId}", userId, roomId)
            .param("content", content)).andExpect(status().isCreated());

        Mockito.verify(chatMessageService).sentMessage(userId, roomId, content);
    }

    @Test
    void createChatRoomTest() throws Exception {
        GroupChatRoomCreateDto chatRoomCreateDto = GroupChatRoomCreateDto.builder()
            .chatName("testName")
            .build();
        ChatRoomDto chatRoomDto = ChatRoomDto.builder().name("testName").build();
        ObjectMapper objectMapper = new ObjectMapper();

        Mockito.when(chatRoomService.createNewChatRoom(Mockito.eq(chatRoomCreateDto))).thenReturn(chatRoomDto);
        mockMvc.perform(post(chatLink + "/create-chatRoom")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(chatRoomCreateDto))).andExpect(status().isCreated()).andReturn();

        Mockito.verify(chatRoomService).createNewChatRoom(Mockito.eq(chatRoomCreateDto));
    }

    @Test
    void chatExistTest() throws Exception {
        Long fistUserId = 1L, secondUserId = 2L;
        Mockito.when(chatMessageService.chatExist(Mockito.eq(fistUserId), Mockito.eq(secondUserId))).thenReturn(
            FriendsChatDto.builder()
                .chatId(1L).build());
        mockMvc.perform(get(chatLink + "/exist/{fistUserId}/{secondUserId}", fistUserId, secondUserId))
            .andExpect(status().isOk());

        Mockito.verify(chatMessageService).chatExist(fistUserId, secondUserId);
    }

    @Test
    void getGroupChatsTest() throws Exception {
        ChatType chatType = ChatType.GROUP;
        Mockito.when(principal.getName()).thenReturn("testmail@gmail.com");
        Participant participant = new Participant();
        Mockito.when(participantService.findByEmail("testmail@gmail.com")).thenReturn(participant);
        List<ChatRoomDto> listOfGroupChatRooms = new ArrayList<>();
        Mockito.when(chatRoomService.findGroupChatRooms(participant, chatType)).thenReturn(listOfGroupChatRooms);
        mockMvc.perform(get(chatLink + "/groups").principal(principal))
            .andExpect(status().isOk());

        Mockito.verify(chatRoomService).findGroupChatRooms(participant, chatType);
    }

    @Test
    void uploadFileTest() throws Exception {
        MockMultipartFile file =
            new MockMultipartFile("file", new byte[1]);
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        Mockito.when(azureFileService.saveFile(file)).thenReturn(chatMessageDto);
        mockMvc.perform(multipart(chatLink + "/upload/file")
            .file(file))
            .andExpect(status().isOk());

        Mockito.verify(azureFileService).saveFile(file);
    }

    @Test
    void uploadVoiceTest() throws Exception {
        MockMultipartFile file =
            new MockMultipartFile("file", new byte[1]);
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        Mockito.when(this.azureFileService.saveVoiceMessage(file)).thenReturn(chatMessageDto);
        mockMvc.perform(multipart(chatLink + "/upload/voice")
            .file(file))
            .andExpect(status().isOk());

        Mockito.verify(this.azureFileService).saveVoiceMessage(file);

    }

    @Test
    void deleteFileTest() throws Exception {
        mockMvc.perform(delete(chatLink + "/delete/file/{fileName}", "fileName"))
            .andExpect(status().isOk());

        Mockito.verify(this.azureFileService).deleteFile("fileName");
    }

    @Test
    void addUserToSystemChatRoomTest() throws Exception {
        Long userId = 1L;
        Mockito.when(chatRoomService.addNewUserToSystemChat(userId)).thenReturn(userId);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post(chatLink + "/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isOk());

        Mockito.verify(chatRoomService).addNewUserToSystemChat(userId);
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
        Mockito.verify(chatMessageService).processMessage(chatMessageDto);
    }

    @Test
    void deleteMessageTest() {
        ChatMessageDto chatMessageDto = ChatMessageDto.builder().id(1L).build();
        chatController.deleteMessage(chatMessageDto);
        Mockito.verify(chatMessageService).deleteMessage(chatMessageDto);
    }

    @Test
    void updateMessageTest() {
        ChatMessageDto chatMessageDto = ChatMessageDto.builder().id(1L).build();
        chatController.updateMessage(chatMessageDto);
        Mockito.verify(chatMessageService).updateMessage(chatMessageDto);
    }

    @Test
    void likeMessageTest() {
        MessageLike messageLike = new MessageLike(1L, 1L);
        chatController.likeMessage(messageLike);
        Mockito.verify(chatMessageService).likeMessage(messageLike);
    }
}
