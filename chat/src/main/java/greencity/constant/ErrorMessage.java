package greencity.constant;

public final class ErrorMessage {
    public static final String USER_NOT_FOUND_BY_EMAIL = "The user does not exist by this email: ";
    public static final String USER_NOT_FOUND_BY_ID = "The user does not exist by this id: ";
    public static final String CHAT_ROOM_NOT_FOUND_BY_ID = "Chat room not found by id: ";
    public static final String FILE_NOT_SAVED = "File hasn't been saved";
    public static final String USER_NOT_BELONG_TO_CHAT = "This user doesnt belong to this chat";
    public static final String USER_NOT_THE_OWNER = "This user is not the owner of the chat";

    private ErrorMessage() {
    }
}
