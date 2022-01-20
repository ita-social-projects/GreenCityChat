package greencity.service.impl;

import greencity.constant.ErrorMessage;
import greencity.dto.ChatMessageDto;
import greencity.dto.ChatMessageResponseDto;
import greencity.dto.MessageLike;
import greencity.dto.PageableDto;
import greencity.entity.ChatMessage;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import greencity.entity.UnreadMessage;
import greencity.enums.MessageStatus;
import greencity.enums.SortOrder;
import greencity.exception.exceptions.ChatRoomNotFoundException;
import greencity.exception.exceptions.UserNotBelongToThisChat;
import greencity.exception.exceptions.UserNotFoundException;
import greencity.repository.ChatMessageRepo;
import greencity.repository.ChatRoomRepo;
import greencity.repository.ParticipantRepo;
import greencity.repository.UnreadMessageRepo;
import greencity.service.AzureFileService;
import greencity.service.ChatMessageService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.swing.*;

/**
 * Implementation of {@link ChatMessageService}.
 */
@Service
@AllArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepo chatMessageRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;
    private final ChatRoomRepo chatRoomRepo;
    private final ParticipantRepo participantRepo;
    private final AzureFileService azureFileService;
    private final UnreadMessageRepo unreadMessageRepo;
    private static final String ROOM_LINK = "/room/";
    private static final String MESSAGE_LINK = "/queue/messages";
    private static final String HEADER_DELETE = "delete";
    private static final String HEADER_UPDATE = "update";

    /**
     * {@inheritDoc}.
     */
    @Override
    public PageableDto<ChatMessageDto> findAllMessagesByChatRoomId(Long chatRoomId, Pageable pageable) {
        ChatRoom chatRoom = chatRoomRepo.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(ErrorMessage.CHAT_ROOM_NOT_FOUND_BY_ID));

        Sort sort =  Sort.by(Sort.Direction.valueOf(SortOrder.DESC.toString()), "createDate");
        pageable  = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ChatMessage> messages = chatMessageRepo.findAllByRoom(chatRoom, pageable);
        List<ChatMessageDto> messageDtos = modelMapper.map(messages.getContent(),
                new TypeToken<List<ChatMessageDto>>() {
                }.getType());
        Collections.reverse(messageDtos);
        return new PageableDto<>(
                messageDtos,
                messages.getTotalElements(),
                messages.getPageable().getPageNumber(),
                messages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processMessage(ChatMessageDto chatMessageDto) {
        ChatMessage message = modelMapper.map(chatMessageDto, ChatMessage.class);
        chatMessageDto = modelMapper.map(chatMessageRepo.save(message), ChatMessageDto.class);
        ArrayList<Participant> participants = new ArrayList<>(
                chatRoomRepo.getPatricipantsByChatRoomId(chatMessageDto.getRoomId()));

        for (Participant current : participants) {
            if (current.getId() != message.getSender().getId()) {
                unreadMessageRepo.save(fillUnreadMessage(message, current));
            }
        }
        ChatMessageResponseDto responseDto = modelMapper.map(chatMessageDto, ChatMessageResponseDto.class);
        responseDto.setCreateDate(chatMessageDto.getCreateDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        participants.stream().forEach(participant -> {
            messagingTemplate.convertAndSend(ROOM_LINK + "/message/chat-messages.", responseDto,
                    Collections.singletonMap("id", participant.getId()));
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMessage(ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = modelMapper.map(chatMessageDto, ChatMessage.class);
        chatMessageRepo.delete(chatMessage);
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_DELETE, new Object());
        messagingTemplate.convertAndSend(
            ROOM_LINK + chatMessageDto.getRoomId() + MESSAGE_LINK, chatMessageDto, headers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMessage(ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = modelMapper.map(chatMessageDto, ChatMessage.class);
        chatMessageRepo.save(chatMessage);
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_UPDATE, new Object());
        messagingTemplate.convertAndSend(
            ROOM_LINK + chatMessageDto.getRoomId() + MESSAGE_LINK, chatMessageDto, headers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatMessageDto findTopByOrderByIdDesc() {
        return modelMapper.map(chatMessageRepo.findTopByOrderByIdDesc(), ChatMessageDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void likeMessage(MessageLike messageLike) {
        if (isLiked(messageLike.getMessageId(), messageLike.getParticipantId())) {
            chatMessageRepo.deleteLikeFromMessage(messageLike.getMessageId(), messageLike.getParticipantId());
        } else {
            chatMessageRepo.addLikeToMessage(messageLike.getMessageId(), messageLike.getParticipantId());
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_UPDATE, new Object());
        ChatMessage chatMessage = chatMessageRepo.findById(messageLike.getMessageId()).get();
        ChatMessageDto chatMessageDto = modelMapper.map(chatMessage,
            ChatMessageDto.class);
        messagingTemplate.convertAndSend(
            ROOM_LINK + chatMessage.getRoom().getId() + MESSAGE_LINK, chatMessageDto, headers);
    }

    @Override
    public void cleanUnreadMessages(Long userId, Long roomId) {
        Optional<ChatRoom> optionalChatRoom = chatRoomRepo.findById(roomId);
        ChatRoom room = optionalChatRoom.get();
        List<Long> messageIds =
            chatMessageRepo.findAllByRoom(room).stream().map(x -> x.getId()).collect(Collectors.toList());
        unreadMessageRepo.cleanUnreadMessage(userId, messageIds);
    }

    private boolean isLiked(Long messageId, Long userId) {
        Long id = chatMessageRepo.getParticipantIdIfLiked(messageId, userId);
        if (id != null) {
            return true;
        } else {
            return false;
        }
    }

    private List<ChatMessageDto> mapListChatMessageDto(List<ChatMessage> messages) {
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for (ChatMessage message : messages) {
            ChatMessageDto dto = modelMapper.map(message, ChatMessageDto.class);
            chatMessageDtos.add(dto);
        }
        return chatMessageDtos;
    }

    private UnreadMessage fillUnreadMessage(ChatMessage message, Participant participant) {
        return UnreadMessage.builder()
            .message(message)
            .participant(participant)
            .status(MessageStatus.UNREAD)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatMessageDto sentMessage(Long userId, Long chatRoomId, String content) {
        ChatRoom chatRoom = chatRoomRepo.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(ErrorMessage.CHAT_ROOM_NOT_FOUND_BY_ID));
        Participant participant = participantRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID));
        if (!chatRoom.getParticipants().contains(participant)) {
            throw new UserNotBelongToThisChat(ErrorMessage.USER_NOT_BELONG_TO_CHAT);
        }
        ChatMessageDto dto = ChatMessageDto.builder()
            .senderId(participant.getId())
            .roomId(chatRoom.getId())
            .content(content)
            .createDate(ZonedDateTime.now())
            .build();
        ChatMessage chatMessage = modelMapper.map(dto, ChatMessage.class);
        processMessage(modelMapper.map(chatMessageRepo.save(chatMessage), ChatMessageDto.class));
        return modelMapper.map(chatMessageRepo.save(chatMessage), ChatMessageDto.class);
    }
}
