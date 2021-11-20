package greencity.service.message;

import greencity.dao.message.MessageDAO;
import greencity.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageDAO messageDAO;

    @Autowired
    public MessageServiceImpl(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    @Override
    @Transactional
    public List<Message> getAllMessagesByChatId(int chatId) {
        return messageDAO.getAllMessagesByChatId(chatId);
    }

    @Override
    @Transactional
    public Message findMessageById(int id) {
        return messageDAO.findMessageById(id);
    }

    @Override
    @Transactional
    public Message changeMessage(Message message) {
        return messageDAO.changeMessage(message);
    }

    @Override
    @Transactional
    public Message addMessage(Message message) {
        return messageDAO.addMessage(message);
    }

    @Override
    @Transactional
    public void deleteMessage(int msgId) {
        messageDAO.deleteMessage(msgId);
    }
}
