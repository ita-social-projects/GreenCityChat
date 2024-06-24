package greencity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 20000)
    @NotBlank
    private String content;
    private ZonedDateTime createDate;
    private String fileName;
    private String fileType;
    private String fileUrl;
}
