package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.*;
import greencity.enums.ChatType;
import greencity.service.*;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @GetMapping("/messages/{room_id}")
    public ResponseEntity<List<ChatMessageDto>> findAllMessages(@PathVariable("room_id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.findAllMessagesByChatRoomId(id));
    }

    /**
     * Method return private room for current user with other user.
     *
     * @param id - id of user
     * @return list of {@link ChatRoomDto}.
     */
    @ApiOperation(value = "Get private room for current user with other user.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatRoomDto.class)
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
     * Create new group chat room.
     *
     * @param groupChatRoomCreateDto of {@link GroupChatRoomCreateDto}
     */
    @ApiOperation(value = "Create group char room.")
    @MessageMapping("/chat/users/create-room")
    public void getGroupChatRoomsWithUsers(GroupChatRoomCreateDto groupChatRoomCreateDto,
                                           Principal principal) {
        chatRoomService.createNewChatRoom(groupChatRoomCreateDto, principal.getName());
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
     * @param chatRoomDto of {@link ChatRoomDto}
     */
    @MessageMapping("/chat/users/leave-room")
    public void leaveRoom(ChatRoomDto chatRoomDto, Principal principal) {
        chatRoomService.leaveChatRoom(chatRoomDto, principal.getName());
    }

    /**
     * Delete chat room.
     *
     * @param chatRoomDto of {@link ChatRoomDto}
     */
    @MessageMapping("/chat/users/delete-room")
    public void deleteChatRoom(ChatRoomDto chatRoomDto) {
        System.out.println("delete");
        chatRoomService.deleteChatRoom(chatRoomDto);
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
     * @param file image to save.
     * @return url of the saved image.
     */
    @ApiOperation(value = "Upload an image.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.CREATED, response = String.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
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
    @ApiOperation(value = "Upload an voice file.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ChatMessageDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
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
     * Method add user to system chat room.
     *
     * @param userId id of new user.
     */
    @ApiOperation(value = "Add user to system chat.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = Long.class)
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
}
