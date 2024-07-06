package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constant.HttpStatuses;
import greencity.dto.*;
import greencity.enums.ChatType;
import greencity.enums.FilesType;
import greencity.service.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.security.Principal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;
import org.springframework.data.domain.Pageable;
import javax.validation.Valid;

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
    @ApiOperation(value = "Get all rooms.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatRoomDto.class, responseContainer = "List")
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
    @ApiOperation(value = "Get all rooms available for current user.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatRoomDto.class, responseContainer = "List")
    })
    @GetMapping("/rooms/visible")
    public ResponseEntity<List<ChatRoomDto>> findAllVisibleRooms(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findAllVisibleRooms(principal.getName()));
    }

    /**
     * Retrieves a list of chat rooms associated with the specified tariff ID.
     *
     * @param tariffId The ID of the tariff for which to retrieve chat rooms.
     * @return A ResponseEntity containing a list of ChatRoomDto objects and an OK
     *         status if successful.
     */
    @GetMapping("/tariffs/{tariffId}")
    public ResponseEntity<List<ChatRoomDto>> findAllChatsByTariffId(@PathVariable Long tariffId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findAllChatsByTariffId(tariffId));
    }

    /**
     * Method return all message by room id.
     *
     * @param id id of room.
     * @return list of {@link ChatMessageDto}.
     */
    @ApiOperation(value = "Get all messages by room id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatMessageDto.class,
            responseContainer = "List"),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @ApiPageable
    @GetMapping("/messages/{room_id}")
    public ResponseEntity<PageableDto<ChatMessageWithFileDto>> findAllMessages(
        @ApiIgnore Pageable pageable,
        @PathVariable("room_id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.findAllMessagesByChatRoomId(id, pageable));
    }

    /**
     * Method return chat room by id.
     *
     * @param id id of room.
     * @return list of {@link ChatMessageDto}.
     */
    @ApiOperation(value = "Get room by id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatRoomDto.class),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
    @ApiOperation(value = "Get current user.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ParticipantDto.class),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
    @ApiOperation(value = "Get user by name.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ParticipantDto.class,
            responseContainer = "List"),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
    @ApiOperation(value = "Get all chat room by name.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatRoomDto.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
    @ApiOperation(value = "Get last message id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class)
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
        chatRoomService.findPrivateByParticipantsForSockets(createNewChatDto.getLocationsIds(),
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
    @ApiOperation(value = "Get group chats.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatRoomDto.class, responseContainer = "List")
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
     * @param file - image to save.
     */
    @ApiOperation(value = "Upload an image.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ChatMessageWithFileDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
    })
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessageWithFileDto> uploadImage(
        @RequestPart("chatMessageDto") @Valid ChatMessageDto chatMessageDto,
        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatMessageService.sendFile(chatMessageDto, file,
            FilesType.IMAGE));
    }

    /**
     * Method for uploading file.
     *
     * @param file file to save.
     */
    @ApiOperation(value = "Upload file.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ChatMessageWithFileDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
    })
    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessageWithFileDto> uploadFile(
        @RequestPart("chatMessageDto") @Valid ChatMessageDto chatMessageDto,
        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatMessageService.sendFile(chatMessageDto, file,
            FilesType.FILE));
    }

    /**
     * Method for uploading voice file.
     *
     * @param file voice file to save.
     * @return ChatMessageDto of the saved voice file.
     */
    @ApiOperation(value = "Upload voice file.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ChatMessageWithFileDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
    })
    @PostMapping(value = "/upload/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessageWithFileDto> uploadVoice(
        @RequestPart("chatMessageDto") @Valid ChatMessageDto chatMessageDto,
        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(chatMessageService.sendVoiceMessage(chatMessageDto, file));
    }

    /**
     * Method for deleting file.
     *
     * @param fileName - name of file for deleting.
     * @return url of the saved image.
     */
    @ApiOperation(value = "Delete file.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK)
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
     * Handles the update of chat status based on the provided message.
     *
     * @param chatMessageDto The ChatMessageDto containing the information for
     *                       updating the chat status.
     */
    @MessageMapping("/chat/update/status")
    public void updateStatus(ChatMessageDto chatMessageDto) {
    }

    /**
     * Method add user to chat room.
     *
     * @param userId id of new user.
     */
    @ApiOperation(value = "Add user to chat room.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class)
    })
    @PostMapping("/user/{userId}/{chatId}")
    public ResponseEntity<Long> addUserToChatRoom(@PathVariable Long userId, @PathVariable Long chatId) {
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomService.addNewUserToChat(userId, chatId));
    }

    /**
     * Adds an admin to a chat room.
     *
     * @param userId The ID of the user who will be added as an admin to the chat
     *               room.
     * @param chatId The ID of the chat room to which the admin will be added.
     */
    @ApiOperation(value = "Add admin to chat room.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class)
    })
    @PostMapping("/admin/{userId}/{chatId}")
    public void addAdminToChatRoom(@PathVariable Long userId, @PathVariable Long chatId) {
        chatRoomService.addNewAdminToChat(userId, chatId);
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
    @ApiOperation(value = "Sent message")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.CREATED, response = ChatMessageDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
    @ApiOperation(value = "Create new chat room")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = ChatRoomDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping(value = "/create-chatRoom")
    @Deprecated
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
    @ApiOperation(value = "Is there already created conversation between two users")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class)
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

    /**
     * Method to retrieve all locations.
     *
     * @return ResponseEntity containing a list of LocationDto objects and an OK
     *         status if successful.
     */
    @ApiOperation(value = "Get all locations.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = LocationsDto.class, responseContainer = "List")
    })
    @GetMapping("/locations/{userId}")
    public ResponseEntity<List<LocationsDto>> getAllLocations(@PathVariable Long userId) {
        List<LocationsDto> allLocations = chatRoomService.getAllLocationsWithChats(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allLocations);
    }

    /**
     * Retrieves the tariff ID associated with the specified location ID.
     *
     * @param locationId The ID of the location for which to retrieve the tariff ID.
     * @return ResponseEntity containing the tariff ID if found, or appropriate
     *         error response if not found or if there are any issues during the
     *         retrieval process
     */
    @ApiOperation(value = "Get Tariff ID by Location ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping(value = "/tariffs/byLocation/{locationId}")
    public ResponseEntity<Long> getTariffIdByLocationId(@PathVariable("locationId") Long locationId) {
        Long tariffId = chatRoomService.getTariffIdByLocationId(locationId);
        return ResponseEntity.status(HttpStatus.OK).body(tariffId);
    }

    /**
     * Retrieves all active chats for an admin user.
     *
     * @param principal The authenticated principal representing the admin user.
     * @param pageable  The pageable object used for pagination.
     * @return A ResponseEntity containing a PageableDto of ChatRoomDto objects
     *         representing active chats.
     */
    @ApiOperation(value = "Get all active chats for admin")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping(value = "/chats/active")
    @ApiPageable
    public ResponseEntity<PageableDto<ChatRoomDto>> getAllActiveChatsForAdmin(Principal principal,
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.getActiveChatsForAdmin(principal.getName(), pageable));
    }
}
