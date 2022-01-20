package greencity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import greencity.enums.ChatType;
import java.util.List;
import java.util.Set;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ChatRoomDto {
    private Long id;
    private String name;
    private ChatType chatType;
    private Set<ParticipantDto> participants;
    private Long ownerId;
    private Long amountUnreadMessages;
    private String logo;
}
