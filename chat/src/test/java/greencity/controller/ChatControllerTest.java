package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.ChatMessageDto;
import greencity.dto.ChatRoomDto;
import greencity.dto.ParticipantDto;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.service.AzureFileService;
import greencity.service.ChatMessageService;
import greencity.service.ChatRoomService;
import greencity.service.ParticipantService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

//    @Test
//    void findAllMessagesTest() throws Exception {
//        mockMvc.perform(get(chatLink + "/messages/{room_id}", 1))
//            .andExpect(status().isOk());
//
//        verify(chatMessageService).findAllMessagesByChatRoomId(1L);
//    }

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
        List<ParticipantDto> list = new ArrayList<>();
        when(participantService.findAllExceptCurrentUser("testmail@gmail.com")).thenReturn(list);
        List<ParticipantDto> listOfAllParticipantsByQuery = new ArrayList<>();
        when(participantService.findAllParticipantsByQuery(query, "testmail@gmail.com"))
            .thenReturn(listOfAllParticipantsByQuery);

        mockMvc.perform(get(chatLink + "/users/{query}", query)
            .principal(principal)).andExpect(status().isOk());

        if (StringUtils.isEmpty(query)) {
            verify(participantService).findAllExceptCurrentUser("testmail@gmail.com");
        }

        verify(participantService).findAllParticipantsByQuery(query, "testmail@gmail.com");
    }

    @Test
    void getAllChatRoomsBy() throws Exception {
        String query = "testQuery";
        when(principal.getName()).thenReturn("testmail@gmail.com");
        Participant participant = new Participant();
        when(participantService.findByEmail("testmail@gmail.com")).thenReturn(participant);
        List<ChatRoomDto> listOfAllChatRoomsByQuery = new ArrayList<>();
        when(chatRoomService.findAllChatRoomsByQuery(query, participant)).thenReturn(listOfAllChatRoomsByQuery);

        mockMvc.perform(get(chatLink + "/rooms/{query}", query)
            .principal(principal)).andExpect(status().isOk());

        if (StringUtils.isEmpty(query)) {
            when(principal.getName()).thenReturn("test");
            List<ChatRoomDto> list = new ArrayList<>();
            when(chatRoomService.findAllVisibleRooms("test")).thenReturn(list);
            mockMvc.perform(get(chatLink + "/rooms/visible").principal(principal)).andExpect(status().isOk());

            verify(chatRoomService).findAllVisibleRooms("test");
        }
        verify(chatRoomService).findAllChatRoomsByQuery(query, participant);
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
    void uploadFileTest() throws Exception {
        MockMultipartFile file =
            new MockMultipartFile("file", new byte[1]);
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        when(azureFileService.saveFile(file)).thenReturn(chatMessageDto);
        mockMvc.perform(multipart(chatLink + "/upload/file")
            .file(file))
            .andExpect(status().isOk());

        verify(azureFileService).saveFile(file);
    }

    @Test
    void uploadVoiceTest() throws Exception {
        MockMultipartFile file =
            new MockMultipartFile("file", new byte[1]);
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        when(this.azureFileService.saveVoiceMessage(file)).thenReturn(chatMessageDto);
        mockMvc.perform(multipart(chatLink + "/upload/voice")
            .file(file))
            .andExpect(status().isOk());

        verify(this.azureFileService).saveVoiceMessage(file);

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

}
