package greencity.dto;

import java.util.List;
import lombok.*;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class ChatMessageDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private ZonedDateTime createDate;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private List<Long> likedUserId;
}
