package greencity.dto;

import greencity.enums.Role;
import greencity.enums.UserStatus;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class ParticipantDto {
    private Long id;
    private String name;
    private String email;
    private String profilePicture;
    private UserStatus userStatus;
    private List<ChatRoomDto> rooms;
    private Role role;
}
