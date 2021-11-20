package greencity.service.user;

import greencity.dao.user.UserDAO;
import greencity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @Transactional
    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    @Override
    @Transactional
    public User saveOrUpdateUser(User user) {
        return userDAO.saveOrUpdateUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(int id) {
        userDAO.delete(id);
    }
}
