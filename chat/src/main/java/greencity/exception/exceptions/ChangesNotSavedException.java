package greencity.exception.exceptions;

public class ChangesNotSavedException extends RuntimeException {
    /**
     * Constructor for {@link ChangesNotSavedException}.
     *
     * @param message - giving message.
     */
    public ChangesNotSavedException(String message) {
        super(message);
    }
}
