package greencity.service.impl;

import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.enums.ChatType;
import greencity.exception.exceptions.ChatRoomNotFoundException;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;
import greencity.service.ChatRoomService;
import greencity.service.ParticipantService;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatMessageServiceImpl chatMessageService;
    private final ChatRoomRepo chatRoomRepo;
    private final ParticipantService participantService;
    private final ModelMapper modelMapper;
    private final ChatMessageRepo chatMessageRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String ROOM_LINK = "/rooms/user/";
    private static final String HEADER_UPDATE_ROOM = "updateRoom";
    private static final String HEADER_DELETE_ROOM = "deleteRoom";
    private static final String HEADER_LEAVE_ROOM = "leaveRoom";

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

    @Override
    public ChatRoomDto findPrivateByParticipants(Long id, String name) {
        Set<Participant> participants = new LinkedHashSet<>();
        Participant owner = participantService.findByEmail(name);
        participants.add(owner);
        participants.add(participantService.findById(id));
        List<ChatRoom> chatRoom = chatRoomRepo.findByParticipantsAndStatus(participants, participants.size(),
            ChatType.PRIVATE);
        return filterPrivateRoom(chatRoom, participants, owner);
    }

    private ChatRoomDto filterPrivateRoom(List<ChatRoom> chatRooms, Set<Participant> participants, Participant owner) {
        ChatRoom toReturn;
        if (chatRooms.isEmpty()) {
            toReturn = chatRoomRepo.save(
                ChatRoom.builder()
                    .name(participants.stream().map(Participant::getName).collect(Collectors.joining(":")))
                    .owner(owner)
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
            .type(ChatType.GROUP)
            .name(dto.getChatName())
            .logo(dto.getLogo())
            .build());

        return modelMapper.map(room, ChatRoomDto.class);
    }

    @Override
    public void deleteParticipantsFromChatRoom(ChatRoomDto chatRoomDto) {
        ChatRoom room = modelMapper.map(chatRoomDto, ChatRoom.class);
        room.setOwner(participantService.findById(chatRoomDto.getOwnerId()));
        room.setType(chatRoomDto.getChatType());
        Set<Participant> participantToSend = chatRoomRepo.getPatricipantsByChatRoomId(chatRoomDto.getId());
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
    public void deleteChatRoom(ChatRoomDto chatRoomDto) {
        chatRoomRepo.deleteById(chatRoomDto.getId());
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_DELETE_ROOM, new Object());
        for (ParticipantDto participant : chatRoomDto.getParticipants()) {
            messagingTemplate.convertAndSend(ROOM_LINK + participant.getId(), chatRoomDto, headers);
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
    public Long addNewUserToSystemChat(Long userId) {
        chatRoomRepo.findSystemChatRooms()
            .forEach(chatRoom -> chatRoomRepo.addUserToSystemChatRoom(chatRoom.getId(), userId));
        return userId;
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
    public void findPrivateByParticipantsForSockets(Long participantId, Long currentUserId) {
        Set<Participant> participants = new LinkedHashSet<>();
        Participant owner = participantService.findById(currentUserId);
        participants.add(owner);
        participants.add(participantService.findById(participantId));
        List<ChatRoom> chatRoom = chatRoomRepo.findByParticipantsAndStatus(participants, participants.size(),
            ChatType.PRIVATE).stream().peek(
                chat -> chat.setName(chat.getName().replaceAll(owner.getName(), "")
                    .replaceAll(":", "")))
            .collect(Collectors.toList());
        ChatRoomDto chatRoomDto = filterPrivateRoom(chatRoom, participants, owner);

        participants.forEach(participant -> messagingTemplate
            .convertAndSend(ROOM_LINK + "new-chats" + participant.getId(), chatRoomDto));
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
}
