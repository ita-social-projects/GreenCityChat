package greencity.exception.exceptions;

public class UserIsNotAdmin extends RuntimeException {
    public UserIsNotAdmin(String message) {
        super(message);
    }
}
