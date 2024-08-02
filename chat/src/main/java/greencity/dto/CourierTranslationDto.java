package greencity.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierTranslationDto {
    private Long id;
    private String nameUk;
    private String nameEn;
}
