package greencity.dto;

import greencity.enums.ChatStatus;
import greencity.enums.ChatType;
import java.time.ZonedDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChatRoomDto {
    private Long id;
    private String name;
    private ChatType chatType;
    private Set<ParticipantDto> participants;
    private Long ownerId;
    private Long tariffId;
    private ChatStatus chatStatus;
    private Long amountUnreadMessages;
    private String logo;
    private String lastMessage;
    private ZonedDateTime lastMessageDateTime;
}
