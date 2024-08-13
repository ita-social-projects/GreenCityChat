package greencity.dto;

import java.util.List;
import greencity.enums.ChatStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatRoomCreateDto {
    private List<Long> usersId;
    private ChatStatus chatStatus;
    private String chatName;
    private Long ownerId;
    private String logo;
}
