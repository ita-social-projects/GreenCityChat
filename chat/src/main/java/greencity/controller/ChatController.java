package greencity.controller;

import greencity.dto.ChatMessageDto;
import greencity.dto.ChatRoomDto;
import greencity.dto.MessageLike;
import greencity.dto.ParticipantDto;
import greencity.enums.ChatType;
import greencity.service.ChatMessageService;
import greencity.service.ChatRoomService;
import greencity.service.ParticipantService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

import greencity.service.impl.ChatImageServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatRoomService chatRoomService;
    private final ParticipantService participantService;
    private final ChatMessageService chatMessageService;
    private final ChatImageServiceImpl chatImageService;

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
     * {@inheritDoc}
     */
    @GetMapping("/users/{ids}/room/{room_name}")
    public ResponseEntity<List<ChatRoomDto>> getGroupChatRoomsWithUsers(@PathVariable("ids") List<Long> ids,
        @PathVariable("room_name") String chatName,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findGroupByParticipants(ids, principal.getName(), chatName));
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
     * {@inheritDoc}
     */
    @DeleteMapping("/delete/room/{room_id}")
    public ResponseEntity<ChatRoomDto> deleteChatRoom(@PathVariable("room_id") Long roomId, Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.deleteChatRoom(roomId, principal.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @PostMapping("/room/leave")
    public ResponseEntity<ChatRoomDto> leaveChatRoom(@RequestBody ChatRoomDto chatRoomDto, Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.leaveChatRoom(chatRoomDto, principal.getName(), chatRoomDto.getOwnerId()));
    }

    /**
     * {@inheritDoc}
     */
    @PostMapping("/room/manage/participants")
    public ResponseEntity<ChatRoomDto> manageParticipantsChatRoom(@RequestBody ChatRoomDto chatRoomDto,
        Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.manageParticipantsAndNameChatRoom(chatRoomDto, principal.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping("/upload/image")
    public ResponseEntity<String> uploadImage(String encodedString) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(chatImageService.save(encodedString));
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping(value = "/media/{name}", produces = "*/*")
    public byte[] getImageWithMediaType(@PathVariable("name") String name) throws IOException {
        return chatImageService.getByteArrayFromFile(name);
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping(value = "/document/download/{name}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("name") String name) throws IOException {
        Path path =
            Paths.get("C:\\Users\\Besitzer\\IdeaProjects\\GreenCity\\chat\\src\\main\\resources\\chat-images\\" + name);
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment").body(resource);
    }

    /**
     * {@inheritDoc}
     */
    @PostMapping("/upload/file")
    public ResponseEntity<ChatMessageDto> uploadFile(@RequestBody MultipartFile file) throws IOException {
        System.out.println("++++++++++++++++++++++++");
        System.out.println(file.getContentType());
        String fileType = chatImageService.getFilteredFileType(Objects.requireNonNull(file.getContentType()));
        String imageName = chatImageService.saveFileAndGetFileName(file.getBytes(), file.getOriginalFilename());
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setImageName(imageName);
        chatMessageDto.setFileType(fileType);
        return ResponseEntity.status(HttpStatus.OK).body(chatMessageDto);
    }

    /**
     * {@inheritDoc}
     */
    @MessageMapping("/chat")
    public void processMessage(ChatMessageDto chatMessageDto) throws IOException {
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
    @MessageMapping("/chat/like")
    public void likeMessage(MessageLike messageLike) {
        chatMessageService.likeMessage(messageLike);
    }
}
