package greencity.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupChatRoomCreateDto {
    private List<Long> usersId;
    private String chatName;
    private Long ownerId;
}
