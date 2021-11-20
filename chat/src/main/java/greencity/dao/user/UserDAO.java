package greencity.dao.user;

import greencity.entity.User;

public interface UserDAO {
    User getUserById(int id);

    User saveOrUpdateUser(User user);

    void delete(int id);
}
