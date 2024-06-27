package greencity.dto;

import greencity.enums.FilesType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatFileDto {
    private String fileName;
    private FilesType fileType;
    private String fileUrl;
}
