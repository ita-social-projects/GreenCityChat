package greencity.exception.exceptions;

/**
 * Exception that we get when we try saving some object, then we get
 * {@link FileNotSavedException}.
 */
public class FileNotSavedException extends RuntimeException {
    /**
     * Constructor for {@link FileNotSavedException}.
     *
     * @param message - giving message.
     */
    public FileNotSavedException(String message) {
        super(message);
    }
}
