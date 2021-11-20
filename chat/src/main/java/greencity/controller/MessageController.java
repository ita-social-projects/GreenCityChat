package greencity.controller;

import greencity.entity.Message;
import greencity.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/chatId/{chatId}")
    public List<Message> getAllMessagesByChatId(@PathVariable int chatId) {
        return messageService.getAllMessagesByChatId(chatId);
    }

    @GetMapping("/{messageId}")
    public Message findMessageById(@PathVariable int messageId) {
        return messageService.findMessageById(messageId);
    }

    @MessageMapping("/addMessage")
    @SendTo("/message/chat-messages")
    public Message addMessage(@Payload Message message) {
        Message result = messageService.addMessage(message);
        System.out.println(result);
        return result;
    }

    @PutMapping("")
    public Message changeMessage(@RequestBody Message message) {
        return messageService.changeMessage(message);
    }

    @DeleteMapping("/{messageId}")
    public void deleteMessage(@PathVariable int messageId) {
        messageService.deleteMessage(messageId);
    }

}
