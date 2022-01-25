package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder

public class FriendsChatDto {
    private Long chatId;
    private Boolean chatExists;
}
