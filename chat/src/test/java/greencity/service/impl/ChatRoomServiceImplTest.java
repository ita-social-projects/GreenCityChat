package greencity.service.impl;

import greencity.dto.ChatRoomDto;
import greencity.dto.GroupChatRoomCreateDto;
import greencity.dto.LeaveChatDto;
import greencity.dto.ParticipantDto;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.ChatRoomNotFoundException;
import greencity.repository.ChatRoomRepo;
import greencity.service.ParticipantService;
import java.lang.reflect.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceImplTest {
    @InjectMocks
    private ChatRoomServiceImpl chatRoomService;
    @Mock
    private ParticipantService participantService;
    @Mock
    private ChatRoomRepo chatRoomRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private final String email = "test.artur@mail.com";
    Participant expectedParticipant;
    ChatRoom expected;
    ChatRoom expectedToReturn;
    ChatRoomDto expectedDto;
    List<ChatRoomDto> expectedListDto;
    List<ChatRoom> expectedList;
    List<ChatRoom> expectedListEmpty;
    Set<Participant> expectedSet;
    ParticipantDto expectedParticipantDto;

    @BeforeEach
    void init() {
        expectedList = new ArrayList<>();
        expectedListEmpty = new ArrayList<>();
        expectedSet = new LinkedHashSet<>();
        expectedParticipantDto = ParticipantDto.builder()
            .id(1L)
            .email("email")
            .name("name")
            .profilePicture("structure")
            .role(Role.ROLE_USER)
            .userStatus(UserStatus.ACTIVATED)
            .rooms(Collections.singletonList(expectedDto))
            .build();
        expectedParticipant = Participant.builder()
            .id(1L)
            .name("artur")
            .email(email)
            .profilePicture(null)
            .userStatus(UserStatus.ACTIVATED)
            .build();
        expectedSet.add(expectedParticipant);
        expected = ChatRoom.builder()
            .id(1L)
            .name("test")
            .messages(new LinkedList<>())
            .type(ChatType.PRIVATE)
            .participants(new HashSet<>())
            .build();
        expectedList.add(expected);
        expectedToReturn = ChatRoom.builder()
            .name(expectedSet.stream().map(Participant::getName).collect(Collectors.joining(":")))
            .owner(expectedParticipant)
            .messages(new ArrayList<>())
            .participants(expectedSet)
            .type(ChatType.PRIVATE)
            .build();
        expectedDto = ChatRoomDto.builder()
            .id(1L)
            .name("test")
            .chatType(ChatType.PRIVATE)
            .participants(new HashSet<>())
            .build();
    }

//    @Test
//    void findAllByParticipantName() {
//        when(participantService.findByEmail(email)).thenReturn(expectedParticipant);
//        when(chatRoomRepo.findAllByParticipant(expectedParticipant)).thenReturn(expectedList);
//        when(modelMapper.map(expectedList, new TypeToken<List<ChatRoomDto>>() {
//        }.getType())).thenReturn(expectedListDto);
//        List<ChatRoomDto> actual = chatRoomService.findAllByParticipantName(email);
//        assertEquals(expectedListDto, actual);
//    }

    @Test
    void findAllRoomsByParticipantsAndStatus() {
        when(chatRoomRepo.findByParticipantsAndStatus(expectedSet, expectedSet.size(), ChatType.GROUP))
            .thenReturn(expectedList);
        when(modelMapper.map(expectedList, new TypeToken<List<ChatRoomDto>>() {
        }.getType())).thenReturn(expectedListDto);
        List<ChatRoomDto> actual = chatRoomService.findAllRoomsByParticipantsAndStatus(expectedSet, ChatType.GROUP);
        assertEquals(expectedListDto, actual);
    }

    @Test
    void findChatRoomById() {
        when(chatRoomRepo.findById(1L)).thenReturn(Optional.of(expected)).thenThrow(ChatRoomNotFoundException.class);
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);
        ChatRoomDto actual = chatRoomService.findChatRoomById(1L);
        assertEquals(expectedDto, actual);
    }

//    @Test
//    void findAllVisibleRooms() {
//        when(participantService.findByEmail(any())).thenReturn(expectedParticipant);
//        when(chatRoomRepo.findAllByParticipant(expectedParticipant)).thenReturn(expectedListEmpty);
//
//        assertEquals(chatRoomService.findAllVisibleRooms("name"), expectedListEmpty);
//    }

    @Test
    void findPrivateByParticipants() {
        when(participantService.findByEmail(anyString())).thenReturn(expectedParticipant);
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.findByParticipantsAndStatus(any(), any(), any())).thenReturn(expectedListEmpty);
        when(chatRoomRepo.save(any())).thenReturn(expected);
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);

        ChatRoomDto actual = chatRoomService.findPrivateByParticipants(1L, "name");

        assertEquals(expectedDto, actual);
        verify(messagingTemplate).convertAndSend(anyString(), eq(expectedDto), any(Map.class));
    }

    @Test
    public void findGroupByParticipants() {
        when(participantService.findByEmail(anyString())).thenReturn(expectedParticipant);
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.findByParticipantsAndStatus(any(), any(), any())).thenReturn(new ArrayList<>());
        when(chatRoomRepo.save(any())).thenReturn(expected);
        when(modelMapper.map(any(ChatRoom.class), eq(ChatRoomDto.class))).thenReturn(expectedDto);

        ChatRoomDto actual =
            chatRoomService.findGroupByParticipants(Collections.singletonList(1L), "name", "chatName").get(0);

        assertEquals(actual, expectedDto);

    }

//    @Test
//    public void createNewChatRoom() {
//        // FIX TESTS
//        expected.setParticipants(Collections.singleton(expectedParticipant));
//
//        List<Long> userId = Collections.singletonList(1L);
//
//        when(participantService.findById(any())).thenReturn(expectedParticipant);
//        when(chatRoomRepo.findByParticipantsAndStatus(any(), any(), any())).thenReturn(expectedListEmpty);
//        when(chatRoomRepo.save(any())).thenReturn(expected);
//        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);
//        chatRoomService.createNewChatRoom(new GroupChatRoomCreateDto(userId, "chatName", 1L));
//
//        verify(messagingTemplate, times(1))
//            .convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()), eq(expectedDto), any(Map.class));
//    }

    @Test
    public void deleteParticipantsFromChatRoom() {
        when(modelMapper.map(any(ChatRoomDto.class), eq(ChatRoom.class))).thenReturn(expected);
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.getPatricipantsByChatRoomId(any())).thenReturn(Collections.singleton(expectedParticipant));
        when(chatRoomRepo.save(any())).thenReturn(expected);

        chatRoomService.deleteParticipantsFromChatRoom(expectedDto);

        verify(modelMapper, times(1)).map(expected, ChatRoomDto.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()),
            any(), any(Map.class));
    }

    @Test
    public void updateChatRoom() {
        expectedDto.setParticipants(Collections.singleton(expectedParticipantDto));

        when(modelMapper.map(any(ChatRoomDto.class), eq(ChatRoom.class))).thenReturn(expected);
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.save(any())).thenReturn(expected);
        chatRoomService.updateChatRoom(expectedDto);

        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()), any(), any(Map.class));
    }

    @Test
    public void deleteChatRoom() {
        expectedDto.setParticipants(Collections.singleton(expectedParticipantDto));

        chatRoomService.deleteChatRoom(expectedDto);

        verify(chatRoomRepo, times(1)).deleteById(any());
        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()), eq(expectedDto), any(Map.class));
    }

    @Test
    public void leaveChatRoom() {
//        ChatRoomDto chatRoomDto = leaveChatDto.getChatRoomDto();
//        ChatRoom chatRoom = modelMapper.map(chatRoomDto, ChatRoom.class);
//        chatRoom.setOwner(participantService.findById(chatRoomDto.getOwnerId()));
//        chatRoom.setType(ChatType.GROUP);
//        chatRoom.getParticipants().removeIf(participant -> participant.getId().equals(leaveChatDto.getUserId()));
//        chatRoomRepo.save(chatRoom);
//        chatRoomDto = modelMapper.map(chatRoom, ChatRoomDto.class);
//        Map<String, Object> headers = new HashMap<>();
//        headers.put(HEADER_LEAVE_ROOM, new Object());
//        for (ParticipantDto participant : chatRoomDto.getParticipants()) {
//            messagingTemplate.convertAndSend(ROOM_LINK + participant.getId(), chatRoomDto, headers);
//        }
//        LeaveChatDto leaveChatDto = new LeaveChatDto();
//        expectedDto.setParticipants(Collections.singleton(expectedParticipantDto));
//        leaveChatDto.setChatRoomDto(expectedDto);
//        leaveChatDto.setUserId(1L);
//        when(modelMapper.map(expectedDto, ChatRoom.class)).thenReturn(expected);
//
//        when(chatRoomRepo.save())

        // Fix
        LeaveChatDto leaveChatDto = LeaveChatDto.builder()
            .chatRoomDto(expectedDto)
            .userId(1L)
            .build();
        expectedDto.setParticipants(Collections.singleton(expectedParticipantDto));

        when(modelMapper.map(any(), eq(ChatRoom.class))).thenReturn(expected);
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.save(any())).thenReturn(expected);
        when(modelMapper.map(any(), eq(ChatRoomDto.class))).thenReturn(expectedDto);

        chatRoomService.leaveChatRoom(leaveChatDto);

        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()), eq(expectedDto), any(Map.class));
    }

    @Test
    public void findGroupChatRooms() {
        when(chatRoomRepo.findGroupChats(any(), any())).thenReturn(Collections.singletonList(expected));
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);

        ChatRoomDto actual = chatRoomService.findGroupChatRooms(expectedParticipant, ChatType.GROUP).get(0);
        assertEquals(expectedDto, actual);
    }

    @Test
    public void findAllChatRoomsByQuery() {
        when(chatRoomRepo.findAllChatRoomsByQuery(anyString(), any())).thenReturn(Collections.singletonList(expected));
        when(modelMapper.map(any(), any(Type.class))).thenReturn(Collections.singletonList(expectedDto));

        ChatRoomDto actual = chatRoomService.findAllChatRoomsByQuery("query", expectedParticipant).get(0);
        assertEquals(expectedDto, actual);
    }

    @Test
    public void addNewUserToSystemChat() {
        Long id = 1L;
        when(chatRoomRepo.findSystemChatRooms()).thenReturn(Collections.singletonList(expected));
        chatRoomService.addNewUserToSystemChat(id);
        verify(chatRoomRepo).addUserToSystemChatRoom(expected.getId(), id);
    }

}