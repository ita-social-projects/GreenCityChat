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
import greencity.repository.UnreadMessageRepo;
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
    private ChatMessageServiceImpl chatMessageService;
    @Mock
    private RestClientUbs restClientUbs;
    @Mock
    private UnreadMessageRepo unreadMessageRepo;

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
            .participants(new HashSet<>())
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
        when(chatRoomRepo.findById(1L)).thenReturn(Optional.of(expected)).thenThrow(ChatRoomNotFoundException.class);
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);
        ChatRoomDto actual = chatRoomService.findChatRoomById(1L);
        assertEquals(expectedDto, actual);
    }

    @Test
    void findAllVisibleRooms() {
        when(participantService.findByEmail(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.findAllByParticipant(anyLong())).thenReturn(expectedListEmpty);

        assertEquals(chatRoomService.findAllVisibleRooms("name"), expectedListEmpty);
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
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.getParticipantsByChatRoomId(any())).thenReturn(Collections.singleton(expectedParticipant));
        when(chatRoomRepo.save(any())).thenReturn(expected);

        chatRoomService.deleteParticipantsFromChatRoom(expectedDto);

        verify(modelMapper, times(1)).map(expected, ChatRoomDto.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/rooms/user/" + expectedParticipant.getId()),
            any(), any(Map.class));
    }

    @Test
    void updateChatRoom() {
        expectedDto.setParticipants(Collections.singleton(expectedParticipantDto));

        when(modelMapper.map(any(ChatRoomDto.class), eq(ChatRoom.class))).thenReturn(expected);
        when(participantService.findById(any())).thenReturn(expectedParticipant);
        when(chatRoomRepo.save(any())).thenReturn(expected);
        chatRoomService.updateChatRoom(expectedDto);

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
        expectedDto.setParticipants(Collections.singleton(expectedParticipantDto));

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
    void findGroupChatRooms() {
        when(chatRoomRepo.findGroupChats(any(), any())).thenReturn(Collections.singletonList(expected));
        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);

        ChatRoomDto actual = chatRoomService.findGroupChatRooms(expectedParticipant, ChatType.GROUP).get(0);
        assertEquals(expectedDto, actual);
    }

    @Test
    void findAllChatRoomsByQuery() {
        when(chatRoomRepo.findAllChatRoomsByQuery(anyString(), any())).thenReturn(Collections.singletonList(expected));
        when(modelMapper.map(any(), any(Type.class))).thenReturn(Collections.singletonList(expectedDto));

        ChatRoomDto actual = chatRoomService.findAllChatRoomsByQuery("query", expectedParticipant).get(0);
        assertEquals(expectedDto, actual);
    }

    @Test
    void addNewUserToSystemChat() {
        Long expectedChatRoomId = 1L;
        Long expectedParticipantId = 2L;

        chatRoomService.addNewUserToChat(expectedParticipantId, expectedChatRoomId);

        verify(chatRoomRepo).addUserToChatRoom(expectedChatRoomId, expectedParticipantId);
    }

    @Test
    @SneakyThrows
    void mapListChatMessageDto() {
        Method method = ChatRoomServiceImpl.class.getDeclaredMethod("mapListChatMessageDto", List.class);
        method.setAccessible(true);

        when(modelMapper.map(expected, ChatRoomDto.class)).thenReturn(expectedDto);

        List<ChatRoomDto> actual = (List<ChatRoomDto>) method.invoke(chatRoomService, expectedList);

        List<ChatRoomDto> expected = new ArrayList<>();
        expected.add(expectedDto);

        assertEquals(expected, actual);
    }

//    @Test
//    void findPrivateByParticipantsForSockets() throws Exception {
//        when(participantService.findById(1L)).thenReturn(expectedParticipant);
//
//        Participant participant = Participant.builder()
//            .id(2L)
//            .name("Danylo")
//            .email("danylo@mail.com")
//            .profilePicture(null)
//            .userStatus(UserStatus.ACTIVATED)
//            .build();
//        when(participantService.findById(2L)).thenReturn(participant);
//        expectedSet.add(participant);
//        expectedList.add(ChatRoom.builder()
//            .id(1L)
//            .name("test")
//            .messages(new LinkedList<>())
//            .type(ChatType.PRIVATE)
//            .participants(new HashSet<>())
//            .owner(expectedParticipant)
//            .build());
//        when(chatRoomRepo.findByParticipantsAndStatus(expectedSet, expectedSet.size(), ChatType.PRIVATE))
//            .thenReturn(expectedList);
//
//        PowerMockito.when(chatRoomService, "filterPrivateRoom", expectedList, expectedSet, expectedParticipant)
//            .thenReturn(expectedDto);
//
//        chatRoomService.findPrivateByParticipantsForSockets(1L, 2L);
//        verify(messagingTemplate, times(1)).convertAndSend("/rooms/user/new-chats" + 2L, expectedDto);
//    }

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

        chatRoomService.deleteMessagesFromChatRoom(1L, 378L);
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
    void testAddNewAdminToChat() {
        Long userId = 1L;
        Long chatRoomId = 2L;

        chatRoomService.addNewAdminToChat(userId, chatRoomId);

        verify(chatRoomRepo, times(1)).addUserToChatRoom(chatRoomId, userId);
    }

    @Test
    void testGetActiveChatsForAdmin() {
        Pageable pageable = PageRequest.of(0, 20);
        EmployeeWithTariffsDto employeeWithTariffsDto = EmployeeWithTariffsDto.builder()
            .employeeDto(EmployeeDto.builder().build())
            .tariffs(Collections.singletonList(
                GetTariffInfoForEmployeeDto.builder()
                    .id(1L)
                    .hasChat(true)
                    .build()))
            .build();
        List<ChatRoom> chatRooms = Collections.singletonList(
            ChatRoom.builder()
                .id(1L)
                .name("Chat Room 1")
                .messages(Collections.singletonList(
                    ChatMessage.builder()
                        .id(1L)
                        .build()))
                .build());
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, chatRooms.size());
        Set<Long> unreadMessageIds = Collections.singleton(1L);

        when(restClientUbs.getEmployeeByEmail(email)).thenReturn(employeeWithTariffsDto);
        when(chatRoomRepo.findAllChatsByTariffIdPageable(Collections.singletonList(1L), pageable))
            .thenReturn(chatRoomPage);
        when(participantService.findByEmail(email)).thenReturn(Participant.builder().id(1L).build());
        when(unreadMessageRepo.findUnreadMessagesIdByUserId(1L)).thenReturn(unreadMessageIds);
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
        assertEquals(1L, chatRoomDto.getId());
        assertEquals(1L, chatRoomDto.getAmountUnreadMessages());

        verify(restClientUbs, times(1)).getEmployeeByEmail(email);
        verify(chatRoomRepo, times(1)).findAllChatsByTariffIdPageable(Collections.singletonList(1L), pageable);
        verify(participantService, times(1)).findByEmail(email);
        verify(unreadMessageRepo, times(1)).findUnreadMessagesIdByUserId(1L);
        verify(modelMapper, times(1)).map(any(ChatRoom.class), eq(ChatRoomDto.class));
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
        Long id = 1L;

        List<LocationsDto> mockedLocations = Arrays.asList(LocationsDto.builder().id(1L).build());
        when(restClientUbs.getAllLocationsByCourierId(id)).thenReturn(mockedLocations);

        List<LocationsDto> locationsWithChats = chatRoomService.getAllLocationsWithChatsByCourierId(id, id);

        assertEquals(mockedLocations, locationsWithChats);
    }

    @Test
    void testFindPrivateByParticipantsForSocketsGPT() {
        Long locationId = 1L;
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

        Long tariffId = 1L;
        when(restClientUbs.getTariffIdByLocationId(locationId)).thenReturn(tariffId);

        when(chatRoomRepo.existsByUserIdAndTariffId(currentUserId, tariffId)).thenReturn(false);
        when(chatRoomRepo.save(any(ChatRoom.class))).thenReturn(new ChatRoom());

        when(modelMapper.map(any(), eq(ChatRoomDto.class))).thenReturn(expectedRoomDto);

        List<EmployeeWithTariffsDto> employeesWithTariffs = Collections.singletonList(
            EmployeeWithTariffsDto.builder()
                .employeeDto(EmployeeDto.builder().email("employee@example.com").build())
                .build());
        when(restClientUbs.getEmployeesByTariffIdWithChat(tariffId)).thenReturn(employeesWithTariffs);

        chatRoomService.findPrivateByParticipantsForSockets(locationId, currentUserId);

        verify(participantService, times(1)).findById(currentUserId);
        verify(restClientUbs, times(1)).getTariffIdByLocationId(locationId);
        verify(chatRoomRepo, times(1)).existsByUserIdAndTariffId(currentUserId, tariffId);
        verify(chatRoomRepo, times(1)).save(any(ChatRoom.class));
        verify(restClientUbs, times(1)).getEmployeesByTariffIdWithChat(tariffId);
        verify(messagingTemplate, times(1)).convertAndSendToUser(
            "employee@example.com", "/rooms/support", expectedRoomDto);
        verify(messagingTemplate, times(1)).convertAndSend("/rooms/user/new-chats1", expectedRoomDto);
    }

    @Test
    void testGetTariffIdByLocationId() {
        Long locationId = 10L;
        Long expectedTariffId = 15L;

        when(restClientUbs.getTariffIdByLocationId(locationId)).thenReturn(expectedTariffId);

        Long actualTariffId = chatRoomService.getTariffIdByLocationId(locationId);

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
            ChatRoomDto expectedChatRoomDto = expectedChatRoomDtos.get(i);
            ChatRoomDto actualDto = actualChatRoomDtos.get(i);
            assertEquals(expectedChatRoomDto.getId(), actualDto.getId());
            assertEquals(expectedChatRoomDto.getName(), actualDto.getName());
            assertEquals(expectedChatRoomDto.getTariffId(), actualDto.getTariffId());
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
