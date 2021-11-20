package greencity.service.user;

import greencity.entity.User;

public interface UserService {
    User getUserById(int id);

    User saveOrUpdateUser(User user);

    void deleteUser(int id);
}
