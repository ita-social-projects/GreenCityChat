package greencity.service.message;

import greencity.entity.Message;

import java.util.List;

public interface MessageService {
    List<Message> getAllMessagesByChatId(int chatId);

    Message findMessageById(int id);

    Message changeMessage(Message message);

    Message addMessage(Message message);

    void deleteMessage(int msgId);
}