package greencity.dto;


import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetTariffInfoForEmployeeDto {
    private Long id;
    private RegionDto region;
    private List<LocationsDtos> locationsDtos;
    private List<GetReceivingStationDto> receivingStationDtos;
    private CourierTranslationDto courier;
}
