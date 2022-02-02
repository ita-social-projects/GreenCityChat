package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class ChatMessageResponseDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private String createDate;
}
