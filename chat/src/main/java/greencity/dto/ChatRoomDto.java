package greencity.dto;

import greencity.enums.ChatType;
import java.time.ZonedDateTime;
import java.util.Set;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class ChatRoomDto {
    private Long id;
    private String name;
    private ChatType chatType;
    private Set<ParticipantDto> participants;
    private Long ownerId;
    private Long amountUnreadMessages;
    private String logo;
    private String lastMessage;
    private ZonedDateTime lastMessageDateTime;
}
