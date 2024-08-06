package greencity.exception.exceptions;

public class UserIsNotAdminException extends RuntimeException {
    /**
     * Exception thrown when a user is not an admin.
     */
    public UserIsNotAdminException(String message) {
        super(message);
    }
}
