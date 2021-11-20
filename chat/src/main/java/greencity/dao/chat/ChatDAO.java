package greencity.dao.chat;

import greencity.entity.Chat;
import greencity.entity.User;

import java.util.List;

public interface ChatDAO {

    Chat getChatByID(int chatId);

    List<Chat> getAllUsersChats(int userId);

    Chat saveOrUpdateChat(Chat chat);

    void deleteChat(int id);

    User addParticipant(int chatId, int userId);

    void deleteAllChatMessages(int id);
}
