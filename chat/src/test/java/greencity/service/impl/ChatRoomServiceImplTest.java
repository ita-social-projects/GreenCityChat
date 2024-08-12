package greencity.service.impl;

import greencity.client.RestClientUbs;
import greencity.dto.*;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.ChatRoomNotFoundException;
import greencity.exception.exceptions.TariffNotFoundException;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;
import greencity.service.AzureFileService;
import greencity.service.ParticipantService;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import lombok.SneakyThrows;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    @Mock
    private ChatMessageRepo chatMessageRepo;
    @Mock
    private RestClientUbs restClientUbs;
    @Mock
    private AzureFileService azureFileService;

    private final String email = "test.artur@mail.com";
    Participant expectedParticipant;
    ChatRoom expected;
    ChatRoom expectedToReturn;
    ChatRoomDto expectedDto;
    List<ChatRoomDto> expectedListDto;
    List<ChatRoom> expectedList;
    List<ChatRoom> expectedListEmpty;
    List<ChatMessage> expectedChatMessageList;
    Set<Participant> expectedSet;
    ParticipantDto expectedParticipantDto;
    GroupChatRoomCreateDto expectedCreateDto;

    @BeforeEach
    void init() {
        expectedList = new ArrayList<>();
        expectedListEmpty = new ArrayList<>();
        expectedSet = new LinkedHashSet<>();
        expectedChatMessageList = new ArrayList<>();
        expectedListDto = List.of(buildChatRoom(1L), buildChatRoom(2L));
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
            .participants(Set.of(expectedParticipantDto))
            .build();
        expectedChatMessageList.add(ChatMessage.builder().id(1L).room(expected).sender(expectedParticipant).build());
        expectedCreateDto = GroupChatRoomCreateDto.builder().usersId(List.of(1L, 2L, 3L)).ownerId(4L).build();
    }

    private ChatRoomDto buildChatRoom(long id) {
        return ChatRoomDto.builder().id(id).build();
    }

    @Test
    void findAllByParticipantName() {
        when(participantService.findByEmail(email)).thenReturn(expectedParticipant);
        when(chatRoomRepo.findAllByParticipant(expectedParticipant.getId())).thenReturn(expectedList);
        when(modelMapper.map(expectedList, new TypeToken<List<ChatRoomDto>>() {
        }.getType())).thenReturn(expectedListDto);
        when(chatMessageRepo.getLastByRoomId(expected.getId())).thenReturn(expectedChatMessageList);

        List<ChatRoomDto> actual = chatRoomService.findAllByParticipantName(email);
        assertEquals(expectedListDto, actual);
    }

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
        when(participantService.findByEmail(email)).thenReturn(expectedParticipant);
        when(chatRoomRepo.findById(1L)).thenReturn(Optional.of(expected)).thenThrow(ChatRoomNotFoundException.class);
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);
        ChatRoomDto actual = chatRoomService.findChatRoomById(1L, email);
        assertEquals(expectedDto, actual);
    }

    @Test
    void findAllVisibleRooms() {
        ChatRoom chatRoom = ChatRoom.builder()
            .id(1L)
            .name("test")
            .messages(new LinkedList<>())
            .type(ChatType.GROUP)
            .participants(new HashSet<>())
            .build();

        chatRoom.setMessages(expectedChatMessageList);
        when(participantService.findByEmail(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.findAllByParticipant(anyLong())).thenReturn(List.of(chatRoom, new ChatRoom()));
        when(modelMapper.map(chatRoom, ChatRoomDto.class)).thenReturn(expectedDto);
        when(chatMessageRepo.getLastByRoomId(chatRoom.getId())).thenReturn(expectedChatMessageList);

        List<ChatRoomDto> actual = chatRoomService.findAllVisibleRooms("test");
        assertEquals(expectedDto, actual.get(0));
        assertEquals(1, actual.size());
    }

    @Test
    void findGroupByParticipants() {
        when(participantService.findByEmail(anyString())).thenReturn(expectedParticipant);
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.findByParticipantsAndStatus(any(), any(), any())).thenReturn(new ArrayList<>());
        when(chatRoomRepo.save(any())).thenReturn(expected);
        when(modelMapper.map(any(ChatRoom.class), eq(ChatRoomDto.class))).thenReturn(expectedDto);

        ChatRoomDto actual =
            chatRoomService.findGroupByParticipants(Collections.singletonList(1L), "name", "chatName").get(0);

        assertEquals(actual, expectedDto);
    }

    @Test
    void createNewChatRoom() {
        when(participantService.findById(expectedCreateDto.getOwnerId())).thenReturn(expectedParticipant);
        when(chatRoomRepo.save(any(ChatRoom.class))).thenReturn(expectedToReturn);
        when(modelMapper.map(expectedToReturn, ChatRoomDto.class)).thenReturn(expectedDto);

        ChatRoomDto actual = chatRoomService.createNewChatRoom(expectedCreateDto);
        assertEquals(expectedDto, actual);
    }

    @Test
    void deleteParticipantsFromChatRoom() {
        when(modelMapper.map(any(ChatRoomDto.class), eq(ChatRoom.class))).thenReturn(expected);
        when(chatRoomRepo.getParticipantsByChatRoomId(any())).thenReturn(Collections.singleton(expectedParticipant));
        when(chatRoomRepo.save(any())).thenReturn(expected);
        when(modelMapper.map(any(ChatRoom.class), eq(ChatRoomDto.class))).thenReturn(expectedDto);

        chatRoomService.deleteParticipantsFromChatRoom(expectedDto, 1L);

        verify(modelMapper, times(1)).map(expected, ChatRoomDto.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/rooms/user/" + expectedParticipantDto.getId()),
            any(), any(Map.class));
    }

    @Test
    void updateChatRoom() {
        when(modelMapper.map(any(ChatRoomDto.class), eq(ChatRoom.class))).thenReturn(expected);
        when(chatRoomRepo.save(any())).thenReturn(expected);
        chatRoomService.updateChatRoom(expectedDto, 1L);

        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()), any(), any(Map.class));
    }

    @Test
    void deleteGroupChatTest() {
        expectedDto.setChatType(ChatType.GROUP);
        expectedDto.setOwnerId(1L);
        chatRoomService.deleteChatRoom(1L, expectedDto);

        verify(chatRoomRepo, times(1)).deleteById(any());

        chatRoomService.deleteChatRoom(1L, expectedDto);
    }

    @Test
    void deleteGroupChatThrowsException() {
        expectedDto.setChatType(ChatType.GROUP);
        expectedDto.setOwnerId(1L);
        chatRoomService.deleteChatRoom(1L, expectedDto);

        verify(chatRoomRepo, times(1)).deleteById(any());

        assertThrows(UnsupportedOperationException.class, () -> chatRoomService.deleteChatRoom(2L, expectedDto));
    }

    @Test
    void deletePrivateChatRoom() {
        expectedDto.setOwnerId(1L);

        chatRoomService.deleteChatRoom(1L, expectedDto);

        verify(chatRoomRepo, times(1)).deleteById(any());
        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()), eq(expectedDto), any(Map.class));
    }

    @Test
    void deletePrivateChatRoomThrowsException() {
        expectedDto.setOwnerId(1L);
        expectedDto.setParticipants(Collections.singleton(expectedParticipantDto));

        assertThrows(UnsupportedOperationException.class, () -> chatRoomService.deleteChatRoom(2L, expectedDto));

    }

    @Test
    void leaveChatRoom() {
        when(modelMapper.map(any(), eq(ChatRoom.class))).thenReturn(expected);
        when(chatRoomRepo.save(any())).thenReturn(expected);
        when(modelMapper.map(any(), eq(ChatRoomDto.class))).thenReturn(expectedDto);

        chatRoomService.leaveChatRoom(expectedDto, 1L);

        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()), eq(expectedDto), any(Map.class));
    }

    @Test
    void findGroupChatRooms() {
        when(chatRoomRepo.findChatRoomsByChatType(any(), any())).thenReturn(Collections.singletonList(expected));
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);

        ChatRoomDto actual = chatRoomService.findChatRoomsByChatType(expectedParticipant, ChatType.GROUP).get(0);
        assertEquals(expectedDto, actual);
    }

    @Test
    void findAllChatRoomsByQuery() {
        when(chatRoomRepo.findAllChatRoomsByQuery(anyString(), any())).thenReturn(List.of(expected, new ChatRoom()));
        when(modelMapper.map(any(), any(Type.class))).thenReturn(Collections.singletonList(expectedDto));

        List<ChatRoomDto> actual = chatRoomService.findAllChatRoomsByQuery("query", expectedParticipant);
        assertEquals(expectedDto, actual.get(0));
        assertEquals(1, actual.size());
    }

    @Test
    void addNewUserToSystemChat() {
        Long expectedChatRoomId = 1L;
        Long expectedParticipantId = 2L;

        chatRoomService.addNewUserToChat(expectedParticipantId, expectedChatRoomId);

        verify(chatRoomRepo).addUserToChatRoom(expectedChatRoomId, expectedParticipantId);
    }

    @Test
    void testAddNewAdminToChat() {
        Long userId = 1L;
        Long chatRoomId = 2L;

        chatRoomService.addNewAdminToChat(userId, chatRoomId);

        verify(chatRoomRepo, times(1)).addUserToChatRoom(chatRoomId, userId);
    }

    @Test
    @SneakyThrows
    void mapListChatRoomDtoTest() {
        Method method = ChatRoomServiceImpl.class.getDeclaredMethod("mapListChatRoomDto", List.class);
        method.setAccessible(true);

        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);

        List<ChatRoomDto> actual = (List<ChatRoomDto>) method.invoke(chatRoomService, expectedList);

        List<ChatRoomDto> expected = new ArrayList<>();
        expected.add(expectedDto);

        assertEquals(expected, actual);
    }

    @Test
    void deleteMessagesFromChatRoomTest() {
        Participant owner = Participant.builder()
            .id(378L)
            .build();
        ChatRoom chatRoom = ChatRoom.builder()
            .id(1L)
            .name("TestRoom")
            .owner(owner)
            .participants(Set.of(owner))
            .build();
        ChatRoomDto chatRoomDto = ChatRoomDto.builder()
            .id(1L)
            .name("TestRoom")
            .participants(Set.of(ParticipantDto.builder()
                .id(378L)
                .build()))
            .ownerId(378L)
            .build();
        when(chatRoomRepo.findById(anyLong())).thenReturn(Optional.ofNullable(chatRoom));
        when(modelMapper.map(chatRoom, ChatRoomDto.class)).thenReturn(chatRoomDto);
        when(chatMessageRepo.getAllByRoomId(1L)).thenReturn(expectedChatMessageList);

        chatRoomService.deleteMessagesFromChatRoom(1L, 378L);

        verify(chatMessageRepo, times(1)).delete(any(ChatMessage.class));
    }

    @Test
    void deleteMessagesFromChatRoomTest_MessageWithFile() {
        ChatMessage chatMessage = ChatMessage.builder()
            .id(1L)
            .room(expected)
            .sender(expectedParticipant)
            .fileName("testFileName")
            .build();
        when(chatRoomRepo.findById(anyLong())).thenReturn(Optional.ofNullable(expected));
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);
        when(chatMessageRepo.getAllByRoomId(1L)).thenReturn(List.of(chatMessage));

        chatRoomService.deleteMessagesFromChatRoom(1L, 1L);

        verify(chatMessageRepo, times(1)).delete(any(ChatMessage.class));
        verify(azureFileService, times(1)).deleteFile("testFileName");
    }

    @Test
    void deleteMessagesFromChatRoomThrowsExceptionTest() {
        ChatRoom chatRoom = ChatRoom.builder()
            .id(1L)
            .name("TestRoom")
            .owner(Participant.builder()
                .id(1L)
                .build())
            .participants(Set.of(Participant.builder()
                .id(1L)
                .build()))
            .build();
        ChatRoomDto chatRoomDto = ChatRoomDto.builder()
            .id(1L)
            .name("TestRoom")
            .participants(Set.of(ParticipantDto.builder()
                .id(1L)
                .build()))
            .ownerId(1L)
            .build();
        when(chatRoomRepo.findById(anyLong())).thenReturn(Optional.ofNullable(chatRoom));
        when(modelMapper.map(chatRoom, ChatRoomDto.class)).thenReturn(chatRoomDto);

        assertThrows(UnsupportedOperationException.class, () -> chatRoomService.deleteMessagesFromChatRoom(1L, 378L));
    }

    @Test
    void testGetActiveChatsForAdmin() {
        Long id = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        EmployeeWithTariffsDto employeeWithTariffsDto = EmployeeWithTariffsDto.builder()
            .employeeDto(EmployeeDto.builder().build())
            .tariffs(Collections.singletonList(
                GetTariffInfoForEmployeeDto.builder()
                    .id(id)
                    .hasChat(true)
                    .build()))
            .build();
        List<ChatRoom> chatRooms = Collections.singletonList(
            ChatRoom.builder()
                .id(id)
                .name("Chat Room 1")
                .messages(Collections.singletonList(
                    ChatMessage.builder()
                        .id(id)
                        .build()))
                .build());
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, chatRooms.size());

        when(restClientUbs.getEmployeeByEmail(email)).thenReturn(employeeWithTariffsDto);
        when(chatRoomRepo.findAllChatsByTariffIdPageable(Collections.singletonList(id), pageable))
            .thenReturn(chatRoomPage);
        when(participantService.findByEmail(email)).thenReturn(Participant.builder().id(id).build());
        when(chatRoomRepo.countUnreadMessages(id, id)).thenReturn(id);
        when(modelMapper.map(any(ChatRoom.class), eq(ChatRoomDto.class)))
            .thenAnswer(invocation -> {
                ChatRoom chatRoom = invocation.getArgument(0);
                return ChatRoomDto.builder()
                    .id(chatRoom.getId())
                    .name(chatRoom.getName())
                    .build();
            });

        PageableDto<ChatRoomDto> actual = chatRoomService.getActiveChatsForAdmin(email, pageable);

        assertEquals(1, actual.getTotalElements());
        assertEquals(1, actual.getTotalPages());
        assertEquals(1, actual.getPage().size());
        ChatRoomDto chatRoomDto = actual.getPage().get(0);
        assertEquals("Chat Room 1", chatRoomDto.getName());
        assertEquals(id, chatRoomDto.getId());
        assertEquals(id, chatRoomDto.getAmountUnreadMessages());

        verify(restClientUbs, times(1)).getEmployeeByEmail(email);
        verify(chatRoomRepo, times(1)).findAllChatsByTariffIdPageable(Collections.singletonList(1L), pageable);
        verify(participantService, times(1)).findByEmail(email);
        verify(chatRoomRepo, times(1)).countUnreadMessages(id, id);
        verify(modelMapper, times(1)).map(any(ChatRoom.class), eq(ChatRoomDto.class));
    }

    @Test
    void testGetActiveChatsForAdmin_WithoutEmployee() {
        Pageable pageable = PageRequest.of(0, 20);

        when(restClientUbs.getEmployeeByEmail(email)).thenReturn(null);

        PageableDto<ChatRoomDto> actual = chatRoomService.getActiveChatsForAdmin(email, pageable);
        assertEquals(new PageableDto<>(Collections.emptyList(), 0, 0, 0), actual);

        verify(restClientUbs, times(1)).getEmployeeByEmail(email);
    }

    @Test
    void testGetActiveChatsForAdmin_NoTariffs() {
        EmployeeWithTariffsDto employee = EmployeeWithTariffsDto.builder()
            .employeeDto(null)
            .tariffs(Collections.emptyList())
            .build();
        when(restClientUbs.getEmployeeByEmail(email)).thenReturn(employee);

        PageableDto<ChatRoomDto> result = chatRoomService.getActiveChatsForAdmin(email, Pageable.unpaged());

        assertEquals(Collections.emptyList(), result.getPage());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void testGetAllLocationsWithChatsByCourierId() {
        Long userId = 1L;
        Long courierId = 1L;
        List<LocationsDto> mockedLocations = Arrays.asList(LocationsDto.builder().id(1L).build());
        when(restClientUbs.getAllLocationsByCourierId(anyLong())).thenReturn(mockedLocations);

        List<LocationsDto> locationsWithChats = chatRoomService.getAllLocationsWithChatsByCourierId(userId, courierId);

        assertEquals(mockedLocations, locationsWithChats);
    }

    @Test
    void findSystemChatByParticipantsForSocketsTest() {
        Long tariffId = 1L;
        Long currentUserId = 1L;
        Set<Participant> participants = new LinkedHashSet<>();
        Participant owner = Participant.builder()
            .id(currentUserId)
            .name("Owner")
            .email("owner@example.com")
            .build();
        participants.add(owner);
        ChatRoomDto expectedRoomDto = ChatRoomDto.builder()
            .id(1L)
            .name("Owner")
            .chatType(ChatType.PRIVATE)
            .build();

        when(participantService.findById(currentUserId)).thenReturn(owner);
        when(chatRoomRepo.save(any(ChatRoom.class))).thenReturn(new ChatRoom());
        when(modelMapper.map(any(), eq(ChatRoomDto.class))).thenReturn(expectedRoomDto);

        List<EmployeeWithTariffsDto> employeesWithTariffs = Collections.singletonList(
            EmployeeWithTariffsDto.builder()
                .employeeDto(EmployeeDto.builder().email("employee@example.com").build())
                .build());
        when(restClientUbs.getEmployeesByTariffIdWithChat(tariffId)).thenReturn(employeesWithTariffs);

        chatRoomService.findSystemChatByParticipantsForSockets(tariffId, currentUserId);

        verify(participantService, times(1)).findById(currentUserId);
        verify(chatRoomRepo, times(1)).save(any(ChatRoom.class));
        verify(restClientUbs, times(1)).getEmployeesByTariffIdWithChat(tariffId);
        verify(messagingTemplate, times(1)).convertAndSendToUser(
            "employee@example.com", "/rooms/support", expectedRoomDto);
        verify(messagingTemplate, times(1)).convertAndSend("/rooms/user/new-chats1", expectedRoomDto);
    }

    @Test
    void testGetTariffIdByLocationId() {
        Long locationId = 10L;
        List<Long> expectedTariffId = List.of(15L);

        when(restClientUbs.getTariffIdByLocationId(locationId)).thenReturn(expectedTariffId);

        List<Long> actualTariffId = chatRoomService.getTariffIdByLocationId(locationId);

        assertEquals(expectedTariffId, actualTariffId);
        verify(restClientUbs, times(1)).getTariffIdByLocationId(locationId);
    }

    @Test
    void testFindAllChatsByTariffId() {
        Long tariffId = 1L;
        List<ChatRoom> expectedChatRooms = Arrays.asList(
            ChatRoom.builder().id(1L).name("Chat Room 1").tariffId(tariffId).build(),
            ChatRoom.builder().id(2L).name("Chat Room 2").tariffId(tariffId).build());
        List<ChatRoomDto> expectedChatRoomDtos = Arrays.asList(
            ChatRoomDto.builder().id(1L).name("Chat Room 1").tariffId(tariffId).build(),
            ChatRoomDto.builder().id(2L).name("Chat Room 2").tariffId(tariffId).build());

        when(restClientUbs.checkIfTariffExistsById(tariffId)).thenReturn(true);
        when(chatRoomRepo.findAllChatsByTariffId(tariffId)).thenReturn(expectedChatRooms);
        when(modelMapper.map(any(ChatRoom.class), eq(ChatRoomDto.class)))
            .thenAnswer(invocation -> {
                ChatRoom room = invocation.getArgument(0);
                return ChatRoomDto.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .tariffId(room.getTariffId())
                    .build();
            });

        List<ChatRoomDto> actualChatRoomDtos = chatRoomService.findAllChatsByTariffId(tariffId);

        assertEquals(expectedChatRoomDtos.size(), actualChatRoomDtos.size());
        for (int i = 0; i < expectedChatRoomDtos.size(); i++) {
            ChatRoomDto expectedDto = expectedChatRoomDtos.get(i);
            ChatRoomDto actualDto = actualChatRoomDtos.get(i);
            assertEquals(expectedDto.getId(), actualDto.getId());
            assertEquals(expectedDto.getName(), actualDto.getName());
            assertEquals(expectedDto.getTariffId(), actualDto.getTariffId());
        }
        verify(restClientUbs, times(1)).checkIfTariffExistsById(tariffId);
        verify(chatRoomRepo, times(1)).findAllChatsByTariffId(tariffId);
        verify(modelMapper, times(expectedChatRooms.size())).map(any(ChatRoom.class), eq(ChatRoomDto.class));
    }

    @Test
    void testFindAllChatsByTariffIdTariffNotFoundException() {
        Long tariffId = 1L;
        when(restClientUbs.checkIfTariffExistsById(tariffId)).thenReturn(false);

        assertThrows(TariffNotFoundException.class, () -> chatRoomService.findAllChatsByTariffId(tariffId));

        verify(restClientUbs, times(1)).checkIfTariffExistsById(tariffId);
        verify(chatRoomRepo, never()).findAllChatsByTariffId(tariffId);
        verify(modelMapper, never()).map(any(ChatRoom.class), eq(ChatRoomDto.class));
    }
}
