package greencity.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class GroupChatRoomCreateDto {
    private List<Long> usersId;
    private String chatName;
    private Long ownerId;
    private String logo;
}
