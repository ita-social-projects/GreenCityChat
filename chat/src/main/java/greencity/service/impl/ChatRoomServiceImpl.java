package greencity.service.impl;

import greencity.client.RestClientUbs;
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
import greencity.service.AzureFileService;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepo chatRoomRepo;
    private final ParticipantService participantService;
    private final ModelMapper modelMapper;
    private final ChatMessageRepo chatMessageRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestClientUbs restClientUbs;
    private final AzureFileService azureFileService;
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
        return formattingChatRooms(chatRoomDtos, participant.getId());
    }

    @Override
    public List<ChatRoomDto> findAllVisibleRooms(String name) {
        Participant participant = participantService.findByEmail(name);
        List<ChatRoom> rooms = chatRoomRepo.findAllByParticipant(participant.getId()).stream()
            .filter(chatRoom -> !chatRoom.getMessages().isEmpty() && chatRoom.getType() != null)
            .collect(Collectors.toList());

        List<ChatRoomDto> roomDtos = mapListChatRoomDto(rooms);
        return formattingChatRooms(roomDtos, participant.getId());
    }

    @Override
    public List<ChatRoomDto> findAllRoomsByParticipantsAndStatus(Set<Participant> participants, ChatType chatType) {
        return modelMapper
            .map(chatRoomRepo.findByParticipantsAndStatus(participants, participants.size(), chatType),
                new TypeToken<List<ChatRoomDto>>() {
                }.getType());
    }

    @Override
    public ChatRoomDto findChatRoomById(Long id, String name) {
        ChatRoom chatRoom = chatRoomRepo.findById(id)
            .orElseThrow(() -> new ChatRoomNotFoundException(ErrorMessage.CHAT_ROOM_NOT_FOUND_BY_ID));
        ChatRoomDto chatRoomDto = modelMapper.map(chatRoom, ChatRoomDto.class);
        chatRoomDto.setAmountUnreadMessages(
            chatRoomRepo.countUnreadMessages(participantService.findByEmail(name).getId(), chatRoomDto.getId()));
        return setLastMessageAndLastMessageDateTime(chatRoomDto);
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

        return toReturn.stream()
            .map(room -> modelMapper.map(room, ChatRoomDto.class))
            .map(this::setLastMessageAndLastMessageDateTime)
            .collect(Collectors.toList());
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
            .type(dto.getChatType())
            .chatStatus(dto.getChatStatus())
            .name(dto.getChatName())
            .logo(dto.getLogo())
            .build());
        return modelMapper.map(room, ChatRoomDto.class);
    }

    @Override
    public void deleteParticipantsFromChatRoom(ChatRoomDto chatRoomDto, Long userId) {
        validateChatOwnership(chatRoomDto, userId);
        ChatRoom room = modelMapper.map(chatRoomDto, ChatRoom.class);
        Set<Participant> participantToSend = chatRoomRepo.getParticipantsByChatRoomId(chatRoomDto.getId());
        room = chatRoomRepo.save(room);
        sendToParticipant(modelMapper.map(room, ChatRoomDto.class),
            participantToSend.stream()
                .map(Participant::getId)
                .collect(Collectors.toSet()),
            HEADER_UPDATE_ROOM);
    }

    @Override
    public void updateChatRoom(ChatRoomDto chatRoomDto, Long userId) {
        validateChatOwnership(chatRoomDto, userId);
        ChatRoom room = modelMapper.map(chatRoomDto, ChatRoom.class);
        room = chatRoomRepo.save(room);
        sendToParticipant(modelMapper.map(room, ChatRoomDto.class),
            getParticipantIds(chatRoomDto), HEADER_UPDATE_ROOM);
    }

    @Override
    public void deleteChatRoom(Long userId, ChatRoomDto chatRoomDto) {
        validateChatOwnership(chatRoomDto, userId);
        chatRoomRepo.deleteById(chatRoomDto.getId());
        Set<Long> participantIds = chatRoomDto.getParticipants().stream()
            .map(ParticipantDto::getId)
            .collect(Collectors.toSet());
        sendToParticipant(chatRoomDto, participantIds, HEADER_DELETE_ROOM);
    }

    @Override
    public void leaveChatRoom(ChatRoomDto chatRoomDto, Long userId) {
        ChatRoom chatRoom = modelMapper.map(chatRoomDto, ChatRoom.class);
        chatRoom.getParticipants().removeIf(participant -> participant.getId().equals(userId));
        chatRoomRepo.save(chatRoom);
        chatRoomDto = modelMapper.map(chatRoom, ChatRoomDto.class);
        sendToParticipant(chatRoomDto, getParticipantIds(chatRoomDto), HEADER_LEAVE_ROOM);
    }

    @Override
    public List<ChatRoomDto> findChatRoomsByChatType(Participant participant, ChatType chatType) {
        return formattingChatRooms(chatRoomRepo.findChatRoomsByChatType(participant, chatType).stream()
            .map(room -> modelMapper.map(room, ChatRoomDto.class))
            .collect(Collectors.toList()), participant.getId());
    }

    @Override
    public List<ChatRoomDto> findAllChatRoomsByQuery(String query, Participant participant) {
        List<ChatRoom> rooms = chatRoomRepo.findAllChatRoomsByQuery(query, participant);
        List<ChatRoomDto> roomsDto = modelMapper
            .map(
                rooms.stream()
                    .filter(chatRoom -> !chatRoom.getMessages().isEmpty()
                        && chatRoom.getType() != null)
                    .collect(Collectors.toList()),
                new TypeToken<List<ChatRoomDto>>() {
                }.getType());
        return formattingChatRooms(roomsDto, participant.getId());
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

    private List<ChatRoomDto> mapListChatRoomDto(List<ChatRoom> rooms) {
        List<ChatRoomDto> chatRoomDtos = new ArrayList<>();
        for (ChatRoom room : rooms) {
            ChatRoomDto chatRoomDto = modelMapper.map(room, ChatRoomDto.class);
            chatRoomDtos.add(chatRoomDto);
        }
        return chatRoomDtos;
    }

    @Override
    public void findSystemChatByParticipantsForSockets(Long tariffId, Long currentUserId) {
        Set<Participant> participants = new LinkedHashSet<>();
        Participant owner = participantService.findById(currentUserId);
        ChatRoomDto room;
        participants.add(owner);
        ChatRoom chatRoom = chatRoomRepo.findByUserIdAndTariffId(currentUserId, tariffId);
        if (chatRoom != null) {
            room = modelMapper.map(chatRoom, ChatRoomDto.class);
        } else {
            ChatRoom save = chatRoomRepo.save(
                ChatRoom.builder()
                    .owner(owner)
                    .tariffId(tariffId)
                    .participants(participants)
                    .type(ChatType.SYSTEM)
                    .name(owner.getEmail())
                    .build());

            room = modelMapper.map(save, ChatRoomDto.class);

            Map<String, Object> headers = new HashMap<>();
            headers.put(HEADER_SUPPORT, new Object());
            List<EmployeeWithTariffsDto> employeesByTariffIdWithChat =
                restClientUbs.getEmployeesByTariffIdWithChat(tariffId);

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
        ChatRoom room = chatRoomRepo.findById(roomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(ErrorMessage.CHAT_ROOM_NOT_FOUND_BY_ID));
        ChatRoomDto chatRoomDto = modelMapper.map(room, ChatRoomDto.class);
        validatePrivateChatMembership(chatRoomDto, userId);
        chatMessageRepo.getAllByRoomId(roomId).forEach(chatMessage -> {
            if (chatMessage.getFileName() != null) {
                azureFileService.deleteFile(chatMessage.getFileName());
            }
            chatMessageRepo.delete(chatMessage);
        });
    }

    @Override
    public List<ChatRoomDto> findAllChatsByTariffId(Long tariffId) {
        if (!restClientUbs.checkIfTariffExistsById(tariffId)) {
            throw new TariffNotFoundException("ChatService - Tariff not found with id: " + tariffId);
        }
        var allChatsByTariffId = chatRoomRepo.findAllChatsByTariffId(tariffId);
        return allChatsByTariffId.stream()
            .map(chatRoom -> modelMapper.map(chatRoom, ChatRoomDto.class))
            .map(this::setLastMessageAndLastMessageDateTime)
            .sorted(Comparator.comparing(ChatRoomDto::getLastMessageDateTime,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public List<LocationsDto> getAllLocationsWithChatsByCourierId(Long userId, Long courierId) {
        List<LocationsDto> allLocations = restClientUbs.getAllLocationsByCourierId(courierId);

        allLocations.forEach(location -> {
            ChatRoom chatRoom = chatRoomRepo.findByUserIdAndTariffId(userId,
                location.getTariffsId());
            location.setChat(chatRoom != null ? modelMapper.map(chatRoom, ChatRoomDto.class) : null);
        });

        return allLocations;
    }

    @Override
    public List<Long> getTariffIdByLocationId(Long locationId) {
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
    @Transactional
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

        Page<ChatRoom> activeChatsPage = chatRoomRepo.findAllChatsByTariffIdPageable(tariffIdsWithChat, pageable);

        List<ChatRoomDto> chatRoomDtos = activeChatsPage.getContent().stream()
            .map(chatRoom -> modelMapper.map(chatRoom, ChatRoomDto.class))
            .collect(Collectors.toList());
        return new PageableDto<>(formattingChatRooms(chatRoomDtos, participantService.findByEmail(email).getId()),
            activeChatsPage.getTotalElements(),
            activeChatsPage.getNumber(),
            activeChatsPage.getTotalPages());
    }

    private ChatRoomDto setLastMessageAndLastMessageDateTime(ChatRoomDto chatRoomDto) {
        chatMessageRepo.getLastByRoomId(chatRoomDto.getId()).stream().findFirst()
            .ifPresent(chatMessage -> {
                chatRoomDto.setLastMessage(chatMessage.getContent());
                chatRoomDto.setLastMessageDateTime(chatMessage.getCreateDate());
            });
        return chatRoomDto;
    }

    private List<ChatRoomDto> formattingChatRooms(List<ChatRoomDto> chatRoomDtos, Long participantId) {
        chatRoomDtos.forEach(chatRoomDto -> chatRoomDto.setAmountUnreadMessages(
            chatRoomRepo.countUnreadMessages(participantId, chatRoomDto.getId())));
        return chatRoomDtos.stream()
            .map(this::setLastMessageAndLastMessageDateTime)
            .sorted(Comparator.comparing(ChatRoomDto::getLastMessageDateTime,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
            .collect(Collectors.toList());
    }

    private <T> void sendToParticipant(T object, Set<Long> participantToSendId,
        String headerString) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(headerString, new Object());
        for (Long participantId : participantToSendId) {
            messagingTemplate.convertAndSend(ROOM_LINK + participantId,
                object, headers);
        }
    }

    private void validateChatOwnership(ChatRoomDto chatRoomDto, Long userId) {
        if (chatRoomDto.getChatType() == ChatType.GROUP) {
            validateGroupChatOwnership(chatRoomDto, userId);
        } else if (chatRoomDto.getChatType() == ChatType.PRIVATE) {
            validatePrivateChatMembership(chatRoomDto, userId);
        }
    }

    private void validateGroupChatOwnership(ChatRoomDto chatRoomDto, Long userId) {
        if (!Objects.equals(chatRoomDto.getOwnerId(), userId)) {
            throw new UnsupportedOperationException(ErrorMessage.USER_NOT_THE_OWNER);
        }
    }

    private void validatePrivateChatMembership(ChatRoomDto chatRoomDto, Long userId) {
        boolean isParticipant = chatRoomDto.getParticipants().stream()
            .anyMatch(participantDto -> Objects.equals(participantDto.getId(), userId));
        if (!isParticipant) {
            throw new UnsupportedOperationException(ErrorMessage.USER_NOT_BELONG_TO_CHAT);
        }
    }

    private Set<Long> getParticipantIds(ChatRoomDto chatRoomDto) {
        return chatRoomDto.getParticipants().stream().map(ParticipantDto::getId).collect(Collectors.toSet());
    }
}
