package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageWithFileDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private ZonedDateTime createDate;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Set<UserDto> likes;
}
