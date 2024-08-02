package greencity.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationsDtos {
    private Long locationId;
    private String nameEn;
    private String nameUk;
}
