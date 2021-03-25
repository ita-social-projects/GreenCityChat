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
     * {@inheritDoc}
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> findAllRooms(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findAllByParticipantName(principal.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping("/rooms/visible")
    public ResponseEntity<List<ChatRoomDto>> findAllVisibleRooms(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findAllVisibleRooms(principal.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping("/messages/{room_id}")
    public ResponseEntity<List<ChatMessageDto>> findAllMessages(@PathVariable("room_id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.findAllMessagesByChatRoomId(id));
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<ChatRoomDto> findPrivateRoomWithUser(@PathVariable Long id, Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findPrivateByParticipants(id, principal.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping("/room/{room_id}")
    public ResponseEntity<ChatRoomDto> findRoomById(@PathVariable("room_id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findChatRoomById(id));
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping("/user")
    public ResponseEntity<ParticipantDto> getCurrentUser(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(participantService.getCurrentParticipantByEmail(principal.getName()));
    }

    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = String.class),
        @ApiResponse(code = 303, message = HttpStatuses.SEE_OTHER),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 500, message = HttpStatuses.INTERNAL_SERVER_ERROR),
    })
    @PostMapping("/upload/file")
    public ResponseEntity<ChatMessageDto> uploadFile(@RequestBody MultipartFile file) throws IOException {
        ChatMessageDto chatMessageDto = azureFileService.saveFile(file);
        return ResponseEntity.status(HttpStatus.OK).body(chatMessageDto);
    }

    /**
     * {@inheritDoc}
     */
    @PostMapping("/upload/voice")
    public ResponseEntity<ChatMessageDto> uploadVoice(@RequestBody MultipartFile file) {
        ChatMessageDto chatMessageDto = this.azureFileService.saveVoiceMessage(file);
        return ResponseEntity.status(HttpStatus.OK).body(chatMessageDto);
    }

    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
