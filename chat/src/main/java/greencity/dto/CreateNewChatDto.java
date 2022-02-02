package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class CreateNewChatDto {
    private Long currentUserId;
    private Long participantsIds;
}
