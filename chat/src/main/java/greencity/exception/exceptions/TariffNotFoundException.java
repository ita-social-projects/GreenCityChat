package greencity.exception.exceptions;

public class TariffNotFoundException extends RuntimeException {
    /**
     * Default constructor.
     */
    public TariffNotFoundException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public TariffNotFoundException(String message) {
        super(message);
    }
}
