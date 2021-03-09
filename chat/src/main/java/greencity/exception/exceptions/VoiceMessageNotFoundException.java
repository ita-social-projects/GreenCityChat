package greencity.exception.exceptions;

/**
 * Exception that we get when we send request to find voice message file by it's
 * parameters and there is no element, then we get
 * {@link VoiceMessageNotFoundException}.
 */
public class VoiceMessageNotFoundException extends RuntimeException {
    /**
     * Constrictor.
     */
    public VoiceMessageNotFoundException(String message) {
        super(message);
    }
}
