package greencity.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveChatDto {
    private ChatRoomDto chatRoomDto;
    private Long userId;
}
