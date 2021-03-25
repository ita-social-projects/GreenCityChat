package greencity.exception.exceptions;

/**
 * Exception that we get when we try get some file but this file does not exist.
 */
public class FileNotFoundException extends RuntimeException {
    /**
     * Constructor for {@link FileNotFoundException}.
     *
     * @param message - giving message.
     */
    public FileNotFoundException(String message) {
        super(message);
    }
}
