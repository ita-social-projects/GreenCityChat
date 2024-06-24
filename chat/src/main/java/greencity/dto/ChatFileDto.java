package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class ChatFileDto {
    private String fileName;
    private String fileType;
    private String fileUrl;
}
