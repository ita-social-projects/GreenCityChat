package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class LocationsDto {
    private Long id;
    private String locationStatus;
    private String regionNameUk;
    private String regionNameEn;
    private Double latitude;
    private Double longitude;
    private String nameUk;
    private String nameEn;
    private Long chatId;
}
