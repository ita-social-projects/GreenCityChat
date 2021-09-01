package greencity.exception.exceptions;

import greencity.entity.Participant;

/**
 * Exception that we get when we send request to find {@link Participant} than
 * not belong to some chat {@link Participant}.
 */
public class UserNotBelongToThisChat extends RuntimeException {
    /**
     * Constructor for UserNotBelongToThisChat.
     *
     * @param message - giving message.
     */
    public UserNotBelongToThisChat(String message) {
        super(message);
    }
}
