package greencity.controller;

import greencity.entity.Chat;
import greencity.entity.User;
import greencity.service.chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{chatId}")
    public Chat getChatById(@PathVariable int chatId) {
        return chatService.getChatByID(chatId);
    }

    @GetMapping("/userId/{userId}")
    public List<Chat> getAllUserChatsById(@PathVariable int userId) {
        return chatService.getAllUsersChats(userId);
    }

    @PostMapping("")
    public Chat createChat(@RequestBody Chat chat) {
        return chatService.saveChat(chat);
    }

    @MessageMapping("/{chatId}/participant")
    @SendTo("/message/new-participant")
    public User addParticipant(@DestinationVariable int chatId, @Payload int userId) {
        System.out.println("chatID: " + chatId + " userID" + userId);
        User result = chatService.addParticipant(chatId, userId);
        System.out.println("result: " + result);
        return result;
    }

    @PutMapping("")
    public Chat updateChat(@RequestBody Chat chat) {
        return chatService.updateChat(chat);
    }

    @DeleteMapping("/{chatId}")
    public void deleteChat(@PathVariable int chatId) {
        chatService.deleteChat(chatId);
    }
}
