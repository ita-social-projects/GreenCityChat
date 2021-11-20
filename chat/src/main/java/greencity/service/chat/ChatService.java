package greencity.service.chat;

import greencity.entity.Chat;
import greencity.entity.User;

import java.util.List;

public interface ChatService {

    Chat getChatByID(int chatId);

    List<Chat> getAllUsersChats(int userId);

    Chat saveChat(Chat chat);

    Chat updateChat(Chat chat);

    User addParticipant(int chatId, int userId);

    void deleteChat(int id);
}
