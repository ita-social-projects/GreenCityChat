package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constant.HttpStatuses;
import greencity.dto.*;
import greencity.enums.ChatType;
import greencity.service.*;

import java.security.Principal;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private static final String WAV = ".wav";
    private final ChatRoomService chatRoomService;
    private final ParticipantService participantService;
    private final ChatMessageService chatMessageService;
    private final AzureFileService azureFileService;

    /**
     * Method return all rooms.
     *
     * @return list of {@link ChatRoomDto}.
     */
    @Operation(summary = "Get all rooms.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> findAllRooms(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findAllByParticipantName(principal.getName()));
    }

    /**
     * Method return all rooms available for current user.
     *
     * @return list of {@link ChatRoomDto}.
     */
    @Operation(summary = "Get all rooms available for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping("/rooms/visible")
    public ResponseEntity<List<ChatRoomDto>> findAllVisibleRooms(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findAllVisibleRooms(principal.getName()));
    }

    /**
     * Method return all message by room id.
     *
     * @param id id of room.
     * @return list of {@link ChatMessageDto}.
     */
    @Operation(summary = "Get all messages by room id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiPageable
    @GetMapping("/messages/{room_id}")
    public ResponseEntity<PageableDto<ChatMessageDto>> findAllMessages(
        @Parameter(hidden = true) Pageable pageable,
        @PathVariable("room_id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.findAllMessagesByChatRoomId(id, pageable));
    }

    /**
     * Method return private room for current user with other user.
     *
     * @param id - id of user
     * @return list of {@link ChatRoomDto}.
     */
    @Operation(summary = "Get private room for current user with other user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ChatRoomDto.class)))
    })
    @GetMapping("/user/{id}")
    public ResponseEntity<ChatRoomDto> findPrivateRoomWithUser(@PathVariable Long id, Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findPrivateByParticipants(id, principal.getName()));
    }

    /**
     * Method return chat room by id.
     *
     * @param id id of room.
     * @return list of {@link ChatMessageDto}.
     */
    @Operation(summary = "Get room by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ChatRoomDto.class))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/room/{room_id}")
    public ResponseEntity<ChatRoomDto> findRoomById(@PathVariable("room_id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findChatRoomById(id));
    }

    /**
     * Method clean unread messages.
     */

    @DeleteMapping("/room/{user_id}/{room_id}")
    public void cleanUnreadMessages(@PathVariable("user_id") Long userId, @PathVariable("room_id") Long roomId) {
        chatMessageService.cleanUnreadMessages(userId, roomId);
    }

    /**
     * Method return current user.
     *
     * @return list of {@link ChatMessageDto}.
     */
    @Operation(summary = "Get current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ChatRoomDto.class))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/user")
    public ResponseEntity<ParticipantDto> getCurrentUser(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(participantService.getCurrentParticipantByEmail(principal.getName()));
    }

    /**
     * Method return user by name.
     *
     * @return list of {@link ChatMessageDto}.
     */
    @Operation(summary = "Get user by name.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ParticipantDto.class))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping(value = {"/users", "/users/{query}"})
    public ResponseEntity<List<ParticipantDto>> getAllParticipantsBy(
        @PathVariable(required = false, value = "query") String query, Principal principal) {
        if (StringUtils.isEmpty(query)) {
            return ResponseEntity.status(HttpStatus.OK)
                .body(participantService.findAllExceptCurrentUser(principal.getName()));
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body(participantService.findAllParticipantsByQuery(query, principal.getName()));
    }

    /**
     * Method return chat room by name.
     *
     * @return list of {@link ChatMessageDto}.
     */
    @Operation(summary = "Get all chat room by name.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ChatRoomDto.class))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping(value = {"/rooms", "/rooms/{query}"})
    public ResponseEntity<List<ChatRoomDto>> getAllChatRoomsBy(
        @PathVariable(required = false, value = "query") String query, Principal principal) {
        if (StringUtils.isEmpty(query)) {
            return this.findAllVisibleRooms(principal);
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findAllChatRoomsByQuery(query, participantService.findByEmail(principal.getName())));
    }

    /**
     * Method return last message id.
     *
     * @return list of {@link ChatMessageDto}.
     */
    @Operation(summary = "Get last message id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping("/last/message")
    public ResponseEntity<Long> getLastId() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.findTopByOrderByIdDesc().getId());
    }

    /**
     * Delete participants from group chat room.
     *
     * @param chatRoomDto of {@link ChatRoomDto}
     */
    @MessageMapping("/chat/users/delete-participants-room")
    public void deleteParticipantsFromChatRoom(ChatRoomDto chatRoomDto) {
        chatRoomService.deleteParticipantsFromChatRoom(chatRoomDto);
    }

    /**
     * Method return private chat for current user..
     */
    @MessageMapping("/chat/user")
    public void createNewPrivateChatIfNotExist(@RequestBody CreateNewChatDto createNewChatDto) {
        chatRoomService.findPrivateByParticipantsForSockets(createNewChatDto.getParticipantsIds(),
            createNewChatDto.getCurrentUserId());
    }

    /**
     * Add participants from group chat room.
     *
     * @param chatRoomDto of {@link ChatRoomDto}
     */
    @MessageMapping("/chat/users/update-room")
    public void addParticipantsToChatRoom(ChatRoomDto chatRoomDto) {
        chatRoomService.updateChatRoom(chatRoomDto);
    }

    /**
     * Delete current user from group chat room.
     *
     * @param leaveChatDto of {@link LeaveChatDto}
     */
    @MessageMapping("/chat/users/leave-room")
    public void leaveRoom(LeaveChatDto leaveChatDto) {
        chatRoomService.leaveChatRoom(leaveChatDto);
    }

    /**
     * Delete chat room.
     *
     * @param chatRoomDto of {@link ChatRoomDto}
     */
    @MessageMapping("/chat/users/{owner_id}/delete-room")
    public void deleteChatRoom(@PathVariable long id, ChatRoomDto chatRoomDto) {
        System.out.println("delete");
        chatRoomService.deleteChatRoom(id, chatRoomDto);
    }

    /**
     * Method return group chats.
     *
     * @return list of {@link ChatMessageDto}.
     */
    @Operation(summary = "Get group chats.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ChatRoomDto.class)))
    })
    @GetMapping("/groups")
    public ResponseEntity<List<ChatRoomDto>> getGroupChats(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findGroupChatRooms(participantService.findByEmail(principal.getName()),
                ChatType.GROUP));
    }

    /**
     * Method for uploading an image.
     *
     * @param file image to save.
     * @return url of the saved image.
     */
    @Operation(summary = "Upload an image.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
    })
    @PostMapping("/upload/file")
    public ResponseEntity<ChatMessageDto> uploadFile(@RequestBody MultipartFile file) {
        ChatMessageDto chatMessageDto = azureFileService.saveFile(file);
        return ResponseEntity.status(HttpStatus.OK).body(chatMessageDto);
    }

    /**
     * Method for uploading an voice file.
     *
     * @param file voice file to save.
     * @return url of the saved image.
     */
    @Operation(summary = "Upload an voice file.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = ChatMessageDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
    })
    @PostMapping("/upload/voice")
    public ResponseEntity<ChatMessageDto> uploadVoice(@RequestBody MultipartFile file) {
        ChatMessageDto chatMessageDto = this.azureFileService.saveVoiceMessage(file);
        return ResponseEntity.status(HttpStatus.OK).body(chatMessageDto);
    }

    /**
     * Method for deleting file.
     *
     * @param fileName - name of file for deleting.
     * @return url of the saved image.
     */
    @Operation(summary = "Delete file.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @DeleteMapping("/delete/file/{fileName}")
    public ResponseEntity<HttpStatus> deleteFile(@PathVariable("fileName") String fileName) {
        this.azureFileService.deleteFile(fileName);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * {@inheritDoc}
     */
    @MessageMapping("/chat")
    public void processMessage(ChatMessageDto chatMessageDto) {
        chatMessageService.processMessage(chatMessageDto);
    }

    /**
     * {@inheritDoc}
     */
    @MessageMapping("/chat/delete")
    public void deleteMessage(ChatMessageDto chatMessageDto) {
        chatMessageService.deleteMessage(chatMessageDto);
    }

    /**
     * {@inheritDoc}
     */
    @MessageMapping("/chat/update")
    public void updateMessage(ChatMessageDto chatMessageDto) {
        chatMessageService.updateMessage(chatMessageDto);
    }

    /**
     * Method add user to system chat room.
     *
     * @param userId id of new user.
     */
    @Operation(summary = "Add user to system chat.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @PostMapping("/user")
    public ResponseEntity<Long> addUserToSystemChatRoom(@RequestBody Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomService.addNewUserToSystemChat(userId));
    }

    /**
     * {@inheritDoc}
     */
    @MessageMapping("/chat/like")
    public void likeMessage(MessageLike messageLike) {
        chatMessageService.likeMessage(messageLike);
    }

    /**
     * Method for send a message.
     *
     * @param userId of user.
     * @param roomId of room
     * @return url of the send message.
     */
    @Operation(summary = "Sent message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = ChatMessageDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/sent-message/{userId}/{roomId}")
    public ResponseEntity<ChatMessageDto> sentMessage(
        @Valid @PathVariable("userId") Long userId,
        @Valid @PathVariable("roomId") Long roomId,
        @RequestParam String content) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(chatMessageService.sentMessage(userId, roomId, content));
    }

    /**
     * Method for create a new chat.
     */
    @Operation(summary = "Create new chat room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = ChatRoomDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping(value = "/create-chatRoom")
    public ResponseEntity<ChatRoomDto> createChatRoom(
        @Valid @RequestBody GroupChatRoomCreateDto dto) {
        chatRoomService.createNewChatRoom(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Method return if there is already created conversation between two users.
     *
     * @return {@link Boolean}.
     */
    @Operation(summary = "Is there already created conversation between two users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    })
    @GetMapping("/exist/{fistUserId}/{secondUserId}")
    public ResponseEntity<FriendsChatDto> chatExist(@PathVariable Long fistUserId, @PathVariable Long secondUserId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.chatExist(fistUserId, secondUserId));
    }

    /**
     * Method deletes all messages from chatroom.
     *
     * @param userId of user
     * @param chatId of chatroom
     * @return
     */
    @DeleteMapping("/room/{userId}/{chatId}/delete")
    public ResponseEntity deleteAllMessagesFromChatRoom(@PathVariable Long userId,
        @PathVariable Long chatId) {
        chatRoomService.deleteMessagesFromChatRoom(chatId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
