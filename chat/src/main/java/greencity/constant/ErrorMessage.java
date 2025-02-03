package greencity.constant;

public final class ErrorMessage {
    public static final String USER_NOT_FOUND_BY_EMAIL = "The user does not exist by this email: ";
    public static final String USER_NOT_FOUND_BY_ID = "The user does not exist by this id: ";
    public static final String CHAT_ROOM_NOT_FOUND_BY_ID = "Chat room not found by id: ";
    public static final String FILE_NOT_SAVED = "File hasn't been saved";
    public static final String USER_NOT_BELONG_TO_CHAT = "This user doesnt belong to this chat";
    public static final String USER_NOT_THE_OWNER = "This user is not the owner of the chat";
    public static final String USER_IS_NOT_ADMIN = "This user is not the admin";
    public static final String CHAT_MESSAGE_NOT_FOUND_BY_ID = "Chat message not found by id: ";
    public static final String CHAT_MESSAGE_CANNOT_BE_EMPTY = "Message content cannot be empty!";
    public static final String WARNING_GIT_DIRECTORY_NOT_FOUND =
        "WARNING: .git directory not found. Git commit info will be unavailable.";
    public static final String GIT_REPOSITORY_NOT_INITIALIZED =
        "Git repository not initialized. Commit info is unavailable.";
    public static final String FAILED_TO_FETCH_COMMIT_INFO = "Failed to fetch commit info due to I/O error: ";

    private ErrorMessage() {
    }
}
