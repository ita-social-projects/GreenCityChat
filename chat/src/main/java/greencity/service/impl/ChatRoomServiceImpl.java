package greencity.service.impl;

import greencity.client.RestClientUbs;
import greencity.client.RestClientUser;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.exception.exceptions.ChatRoomNotFoundException;
import greencity.exception.exceptions.TariffNotFoundException;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;
import greencity.repository.UnreadMessageRepo;
import greencity.service.ChatRoomService;
import greencity.service.ParticipantService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatMessageServiceImpl chatMessageService;
    private final ChatRoomRepo chatRoomRepo;
    private final ParticipantService participantService;
    private final ModelMapper modelMapper;
    private final ChatMessageRepo chatMessageRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestClientUbs restClientUbs;
    private final RestClientUser restClientUser;
    private final UnreadMessageRepo unreadMessageRepo;
    private static final String ROOM_LINK = "/rooms/user/";
    private static final String SUPPORT_LINK = "/rooms/support";
    private static final String HEADER_UPDATE_ROOM = "updateRoom";
    private static final String HEADER_DELETE_ROOM = "deleteRoom";
    private static final String HEADER_LEAVE_ROOM = "leaveRoom";
    private static final String HEADER_SUPPORT = "support";

    @Override
    public List<ChatRoomDto> findAllByParticipantName(String name) {
        Participant participant = participantService.findByEmail(name);
        List<ChatRoom> chatRooms = chatRoomRepo.findAllByParticipant(participant.getId()).stream()
            .peek(chatRoom -> chatRoom.setName(chatRoom.getName().replaceAll(participant.getName(), "")
                .replaceAll(":", "")))
            .collect(Collectors.toList());
        List<ChatRoomDto> chatRoomDtos = modelMapper.map(chatRooms, new TypeToken<List<ChatRoomDto>>() {
        }.getType());
        chatRoomDtos.forEach(chatRoom -> chatMessageRepo.getLastByRoomId(chatRoom.getId()).stream().findFirst()
            .ifPresent(chatMessage -> {
                chatRoom.setLastMessage(chatMessage.getContent());
                chatRoom.setLastMessageDateTime(chatMessage.getCreateDate());
            }));
        return chatRoomDtos;
    }

    @Override
    public List<ChatRoomDto> findAllVisibleRooms(String name) {
        Participant participant = participantService.findByEmail(name);
        List<ChatRoom> rooms = chatRoomRepo.findAllByParticipant(participant.getId()).stream()
            .filter(chatRoom -> !chatRoom.getMessages().isEmpty() && chatRoom.getType().equals(ChatType.PRIVATE)
                || chatRoom.getType().equals(ChatType.GROUP) || chatRoom.getType().equals(ChatType.SYSTEM))
            .collect(Collectors.toList());

        List<ChatRoomDto> roomDtos = mapListChatMessageDto(rooms);
        roomDtos
            .forEach(x -> x.setAmountUnreadMessages(chatRoomRepo.countUnreadMessages(participant.getId(), x.getId())));
        return roomDtos;
    }

    @Override
    public List<ChatRoomDto> findAllRoomsByParticipantsAndStatus(Set<Participant> participants, ChatType chatType) {
        return modelMapper
            .map(chatRoomRepo.findByParticipantsAndStatus(participants, participants.size(), chatType),
                new TypeToken<List<ChatRoomDto>>() {
                }.getType());
    }

    @Override
    public ChatRoomDto findChatRoomById(Long id) {
        ChatRoom chatRoom = chatRoomRepo.findById(id)
            .orElseThrow(() -> new ChatRoomNotFoundException(ErrorMessage.CHAT_ROOM_NOT_FOUND_BY_ID));
        return modelMapper.map(chatRoom, ChatRoomDto.class);
    }

    private ChatRoomDto filterPrivateRoom(List<ChatRoom> chatRooms, Set<Participant> participants, Participant owner,
        Long tariffId) {
        ChatRoom toReturn;
        if (chatRooms.isEmpty()) {
            toReturn = chatRoomRepo.save(
                ChatRoom.builder()
                    .name(participants.stream().map(Participant::getName).collect(Collectors.joining(":")))
                    .owner(owner)
                    .tariffId(tariffId)
                    .participants(participants)
                    .type(ChatType.PRIVATE)
                    .build());
            toReturn.setName(toReturn.getName().replaceAll(owner.getName(), "")
                .replaceAll(":", ""));
        } else {
            toReturn = chatRooms.get(0);
        }
        return modelMapper.map(toReturn, ChatRoomDto.class);
    }

    @Override
    public List<ChatRoomDto> findGroupByParticipants(List<Long> ids, String name, String chatName) {
        Set<Participant> participants = new HashSet<>();
        Participant owner = participantService.findByEmail(name);
        participants.add(owner);
        ids.forEach(id -> participants.add(participantService.findById(id)));
        List<ChatRoom> chatRoom = chatRoomRepo.findByParticipantsAndStatus(participants, participants.size(),
            ChatType.GROUP);
        return filterGroupRoom(chatRoom, participants, chatName, owner);
    }

    private List<ChatRoomDto> filterGroupRoom(List<ChatRoom> chatRoom, Set<Participant> participants,
        String chatName, Participant owner) {
        List<ChatRoom> toReturn = new ArrayList<>();
        if (chatRoom.isEmpty()) {
            toReturn.add(chatRoomRepo.save(
                ChatRoom.builder()
                    .name(chatName)
                    .participants(participants)
                    .owner(owner)
                    .type(ChatType.GROUP)
                    .build()));
        } else {
            toReturn = chatRoom;
        }

        return toReturn.stream().map(room -> modelMapper.map(room, ChatRoomDto.class)).collect(Collectors.toList());
    }

    @Override
    public ChatRoomDto createNewChatRoom(GroupChatRoomCreateDto dto) {
        Participant owner = participantService.findById(dto.getOwnerId());
        Set<Participant> participants = new HashSet<>();
        participants.add(owner);

        dto.getUsersId().forEach(id -> participants.add(participantService.findById(id)));
        ChatRoom room = chatRoomRepo.save(ChatRoom
            .builder()
            .participants(participants)
            .owner(owner)
            .chatStatus(dto.getChatStatus())
            .tariffId(dto.getTariffId())
            .type(ChatType.GROUP)
            .name(dto.getChatName())
            .logo(dto.getLogo())
            .build());

        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_SUPPORT, new Object());
        List<EmployeeWithTariffsDto> employeesByTariffIdWithChat =
            restClientUbs.getEmployeesByTariffIdWithChat(dto.getTariffId());

        employeesByTariffIdWithChat.forEach(employee -> {
            messagingTemplate.convertAndSendToUser(
                employee.getEmployeeDto().getEmail(), SUPPORT_LINK, modelMapper.map(room, ChatRoomDto.class));
            log.info("Notification sent to {}", employee.getEmployeeDto().getEmail());
        });

        return modelMapper.map(room, ChatRoomDto.class);
    }

    @Override
    public void deleteParticipantsFromChatRoom(ChatRoomDto chatRoomDto) {
        ChatRoom room = modelMapper.map(chatRoomDto, ChatRoom.class);
        room.setOwner(participantService.findById(chatRoomDto.getOwnerId()));
        room.setType(chatRoomDto.getChatType());
        Set<Participant> participantToSend = chatRoomRepo.getParticipantsByChatRoomId(chatRoomDto.getId());
        room = chatRoomRepo.save(room);
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_UPDATE_ROOM, new Object());
        for (Participant participant : participantToSend) {
            messagingTemplate.convertAndSend(ROOM_LINK + participant.getId(),
                modelMapper.map(room, ChatRoomDto.class), headers);
        }
    }

    @Override
    public void updateChatRoom(ChatRoomDto chatRoomDto) {
        ChatRoom room = modelMapper.map(chatRoomDto, ChatRoom.class);
        room.setOwner(participantService.findById(chatRoomDto.getOwnerId()));
        room.setType(chatRoomDto.getChatType());
        room = chatRoomRepo.save(room);
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_UPDATE_ROOM, new Object());
        for (ParticipantDto participant : chatRoomDto.getParticipants()) {
            messagingTemplate.convertAndSend(ROOM_LINK + participant.getId(),
                modelMapper.map(room, ChatRoomDto.class), headers);
        }
    }

    @Override
    public void deleteChatRoom(Long userId, ChatRoomDto chatRoomDto) {
        if (chatRoomDto.getChatType() == ChatType.GROUP) {
            if (Objects.equals(chatRoomDto.getOwnerId(), userId)) {
                chatRoomRepo.deleteById(chatRoomDto.getId());
                Map<String, Object> headers = new HashMap<>();
                headers.put(HEADER_DELETE_ROOM, new Object());
                for (ParticipantDto participant : chatRoomDto.getParticipants()) {
                    messagingTemplate.convertAndSend(ROOM_LINK + participant.getId(), chatRoomDto, headers);
                }
            } else {
                throw new UnsupportedOperationException(ErrorMessage.USER_NOT_THE_OWNER);
            }
        } else if (chatRoomDto.getChatType() == ChatType.PRIVATE) {
            if (chatRoomDto.getParticipants().stream()
                .anyMatch(participantDto -> Objects.equals(participantDto.getId(), userId))) {
                chatRoomRepo.deleteById(chatRoomDto.getId());
                Map<String, Object> headers = new HashMap<>();
                headers.put(HEADER_DELETE_ROOM, new Object());
                for (ParticipantDto participant : chatRoomDto.getParticipants()) {
                    messagingTemplate.convertAndSend(ROOM_LINK + participant.getId(), chatRoomDto, headers);
                }
            } else {
                throw new UnsupportedOperationException(ErrorMessage.USER_NOT_BELONG_TO_CHAT);
            }
        }
    }

    @Override
    public void leaveChatRoom(LeaveChatDto leaveChatDto) {
        ChatRoomDto chatRoomDto = leaveChatDto.getChatRoomDto();
        ChatRoom chatRoom = modelMapper.map(chatRoomDto, ChatRoom.class);
        chatRoom.setOwner(participantService.findById(chatRoomDto.getOwnerId()));
        chatRoom.setType(ChatType.GROUP);
        chatRoom.getParticipants().removeIf(participant -> participant.getId().equals(leaveChatDto.getUserId()));
        chatRoomRepo.save(chatRoom);
        chatRoomDto = modelMapper.map(chatRoom, ChatRoomDto.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_LEAVE_ROOM, new Object());
        for (ParticipantDto participant : chatRoomDto.getParticipants()) {
            messagingTemplate.convertAndSend(ROOM_LINK + participant.getId(), chatRoomDto, headers);
        }
    }

    @Override
    public List<ChatRoomDto> findGroupChatRooms(Participant participant, ChatType chatType) {
        return chatRoomRepo.findGroupChats(participant, chatType).stream()
            .map(room -> modelMapper.map(room, ChatRoomDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomDto> findAllChatRoomsByQuery(String query, Participant participant) {
        List<ChatRoom> rooms = chatRoomRepo.findAllChatRoomsByQuery(query, participant);
        return modelMapper
            .map(
                rooms.stream()
                    .filter(chatRoom -> !chatRoom.getMessages().isEmpty()
                        && chatRoom.getType().equals(ChatType.PRIVATE)
                        || chatRoom.getType().equals(ChatType.GROUP)
                        || chatRoom.getType().equals(ChatType.SYSTEM))
                    .collect(Collectors.toList()),
                new TypeToken<List<ChatRoomDto>>() {
                }.getType());
    }

    @Override
    public Long addNewUserToChat(Long userId, Long chatRoomId) {
        chatRoomRepo.addUserToChatRoom(chatRoomId, userId);
        return userId;
    }

    @Override
    public void addNewAdminToChat(Long userId, Long chatRoomId) {
        chatRoomRepo.addUserToChatRoom(chatRoomId, userId);
    }

    private List<ChatRoomDto> mapListChatMessageDto(List<ChatRoom> rooms) {
        List<ChatRoomDto> chatRoomDtos = new ArrayList<>();
        for (ChatRoom room : rooms) {
            ChatRoomDto chatRoomDto = modelMapper.map(room, ChatRoomDto.class);
            chatRoomDtos.add(chatRoomDto);
        }
        return chatRoomDtos;
    }

    @Override
    public void findPrivateByParticipantsForSockets(Long locationId, Long currentUserId) {
        Set<Participant> participants = new LinkedHashSet<>();
        Participant owner = participantService.findById(currentUserId);
        Long tariffIdByLocationId = restClientUbs.getTariffIdByLocationId(locationId);
        ChatRoomDto room;
        participants.add(owner);

        if (chatRoomRepo.existsByUserIdAndTariffId(currentUserId, tariffIdByLocationId)) {
            room = modelMapper.map(chatRoomRepo.findByUserIdAndTariffId(currentUserId, tariffIdByLocationId),
                ChatRoomDto.class);
        } else {
            ChatRoom save = chatRoomRepo.save(
                ChatRoom.builder()
                    .name(participants.stream().map(Participant::getName).collect(Collectors.joining(":")))
                    .owner(owner)
                    .tariffId(tariffIdByLocationId)
                    .participants(participants)
                    .type(ChatType.PRIVATE)
                    .name(owner.getEmail())
                    .build());

            room = modelMapper.map(save, ChatRoomDto.class);

            Map<String, Object> headers = new HashMap<>();
            headers.put(HEADER_SUPPORT, new Object());
            List<EmployeeWithTariffsDto> employeesByTariffIdWithChat =
                restClientUbs.getEmployeesByTariffIdWithChat(tariffIdByLocationId);

            employeesByTariffIdWithChat.forEach(employee -> {
                messagingTemplate.convertAndSendToUser(
                    employee.getEmployeeDto().getEmail(), SUPPORT_LINK, room);
                log.info("Notification sent to {}", employee.getEmployeeDto().getEmail());
            });
        }

        participants.forEach(participant -> messagingTemplate
            .convertAndSend(ROOM_LINK + "new-chats" + participant.getId(), room));
    }

    @Override
    public void deleteMessagesFromChatRoom(Long roomId, Long userId) {
        ChatRoomDto room = findChatRoomById(roomId);
        if (room.getParticipants().stream().anyMatch(participant -> Objects.equals(participant.getId(), userId))) {
            chatMessageRepo.getAllByRoomId(roomId).stream()
                .map(chatMessage -> modelMapper.map(chatMessage, ChatMessageDto.class))
                .forEach(chatMessageService::deleteMessage);
        } else {
            throw new UnsupportedOperationException(ErrorMessage.USER_NOT_BELONG_TO_CHAT);
        }
    }

    @Override
    public List<ChatRoomDto> findAllChatsByTariffId(Long tariffId) {
        if (!restClientUbs.checkIfTariffExistsById(tariffId)) {
            throw new TariffNotFoundException("ChatService - Tariff not found with id: " + tariffId);
        } else {
            var allChatsByTariffId = chatRoomRepo.findAllChatsByTariffId(tariffId);
            return allChatsByTariffId.stream()
                .map(chatRoom -> modelMapper.map(chatRoom, ChatRoomDto.class))
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<LocationsDto> getAllLocationsWithChats(Long userId) {
        List<LocationsDto> allLocations = restClientUbs.getAllLocations();

        allLocations.forEach(location -> {
            Long tariffId = restClientUbs.getTariffIdByLocationId(location.getId());
            // //List<ChatRoom> chatRooms = chatRoomRepo.findByUserIdAndTariffId(userId,
            // tariffId);
            // chatRooms.forEach(chatRoom -> {
            // location.setChatId(chatRoom.getId());
            // });
            ChatRoom chatRoom = chatRoomRepo.findByUserIdAndTariffId(userId,
                tariffId);
            location.setChatId(chatRoom.getId());
        });

        return allLocations;
    }

    @Override
    public Long getTariffIdByLocationId(Long locationId) {
        return restClientUbs.getTariffIdByLocationId(locationId);
    }

    /**
     * Retrieves a pageable list of active chat rooms for the admin associated with
     * the provided email.
     *
     * @param email    The email of the admin.
     * @param pageable Pagination information.
     */
    @Override
    public PageableDto<ChatRoomDto> getActiveChatsForAdmin(String email, Pageable pageable) {
        EmployeeWithTariffsDto employee = restClientUbs.getEmployeeByEmail(email);

        if (employee == null) {
            return new PageableDto<>(Collections.emptyList(), 0, 0, 0);
        }

        List<Long> tariffIdsWithChat = employee.getTariffs().stream()
            .filter(GetTariffInfoForEmployeeDto::getHasChat)
            .map(GetTariffInfoForEmployeeDto::getId)
            .collect(Collectors.toList());

        if (tariffIdsWithChat.isEmpty()) {
            return new PageableDto<>(Collections.emptyList(), 0, 0, 0);
        }

        Set<Long> allUnreadMessageIds =
            unreadMessageRepo.findUnreadMessagesIdByUserId(participantService.findByEmail(email).getId());

        Page<ChatRoom> activeChatsPage = chatRoomRepo.findAllChatsByTariffIdPageable(tariffIdsWithChat, pageable);

        List<ChatRoomDto> chatRoomDtos = activeChatsPage.getContent().stream()
            .map(chatRoom -> getChatRoomDtoWithAmountUnreadMessages(chatRoom, allUnreadMessageIds))
            .sorted(Comparator.comparing(ChatRoomDto::getLastMessageDateTime,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
            .collect(Collectors.toList());

        return new PageableDto<>(chatRoomDtos,
            activeChatsPage.getTotalElements(),
            activeChatsPage.getNumber(),
            activeChatsPage.getTotalPages());
    }

    private ChatRoomDto getChatRoomDtoWithAmountUnreadMessages(ChatRoom chatRoom, Set<Long> allUnreadMessageIds) {
        ChatRoomDto dto = modelMapper.map(chatRoom, ChatRoomDto.class);
        long unreadMessagesCount = chatRoom.getMessages().stream()
            .filter(message -> allUnreadMessageIds.contains(message.getId()))
            .count();
        dto.setAmountUnreadMessages(unreadMessagesCount);
        return dto;
    }
}
