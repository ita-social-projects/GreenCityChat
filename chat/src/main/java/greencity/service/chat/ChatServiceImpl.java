package greencity.service.chat;

import greencity.dao.chat.ChatDAO;
import greencity.entity.Chat;
import greencity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatDAO chatDAO;

    @Autowired
    public ChatServiceImpl(ChatDAO chatDAO) {
        this.chatDAO = chatDAO;
    }

    @Override
    @Transactional
    public Chat getChatByID(int chatId) {
        return chatDAO.getChatByID(chatId);
    }

    @Override
    @Transactional
    public List<Chat> getAllUsersChats(int userId) {
        return chatDAO.getAllUsersChats(userId);
    }

    @Override
    @Transactional
    public Chat saveChat(Chat chat) {
        return chatDAO.saveOrUpdateChat(chat);
    }

    @Override
    @Transactional
    public Chat updateChat(Chat chat) {
        Chat existingChat = chatDAO.getChatByID(chat.getId());
        chat.setParticipants(existingChat.getParticipants());
        return chatDAO.saveOrUpdateChat(chat);
    }

    @Override
    @Transactional
    public User addParticipant(int chatId, int userId) {
        return chatDAO.addParticipant(chatId, userId);
    }

    @Override
    @Transactional
    public void deleteChat(int id) {
        chatDAO.deleteAllChatMessages(id);
        chatDAO.deleteChat(id);
    }
}
