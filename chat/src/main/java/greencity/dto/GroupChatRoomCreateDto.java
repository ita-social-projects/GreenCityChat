package greencity.dto;

import java.util.List;

import greencity.enums.ChatStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatRoomCreateDto {
    private List<Long> usersId;
    private Long tariffId;
    private ChatStatus chatStatus;
    private String chatName;
    private Long ownerId;
    private String logo;
}
