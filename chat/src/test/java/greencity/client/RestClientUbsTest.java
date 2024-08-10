package greencity.client;

import java.util.ArrayList;
import java.util.List;
import greencity.dto.EmployeeWithTariffsDto;
import greencity.dto.LocationsDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestClientUbsTest {

    @InjectMocks
    private RestClientUbs restClientUbs;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Value("http://localhost:8080")
    private String greenCityUbsServerAddress;

    @Value("Bearer token")
    private String accessToken;
    private final HttpHeaders headers = new HttpHeaders();

    @Test
    void checkIfTariffExistsByIdTest() {
        Long tariffInfoId = 1L;
        String url = greenCityUbsServerAddress + "/ubs/check-if-tariff-exists/" + tariffInfoId;
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(httpServletRequest.getHeader("Authorization")).thenReturn(accessToken);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, Boolean.class)).thenReturn(ResponseEntity.ok(true));

        assertTrue(restClientUbs.checkIfTariffExistsById(tariffInfoId));
    }

    @Test
    void getAllLocationsByCourierIdTest() {
        Long courierId = 1L;
        String url = greenCityUbsServerAddress + "/ubs/locationsByCourier/" + courierId;
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<LocationsDto> locations = List.of(new LocationsDto());

        when(httpServletRequest.getHeader("Authorization")).thenReturn(accessToken);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<LocationsDto>>() {
        }))
            .thenReturn(ResponseEntity.ok(locations));

        List<LocationsDto> result = restClientUbs.getAllLocationsByCourierId(courierId);

        assertEquals(1, result.size());
    }

    @Test
    void getEmployeeByEmailTest() {
        String email = "test@example.com";
        String url = greenCityUbsServerAddress + "/admin/ubs-employee/" + email;
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        EmployeeWithTariffsDto employee = new EmployeeWithTariffsDto();

        when(httpServletRequest.getHeader("Authorization")).thenReturn(accessToken);
        when(restTemplate.exchange(url, HttpMethod.GET, entity,
            new ParameterizedTypeReference<EmployeeWithTariffsDto>() {
            }))
                .thenReturn(ResponseEntity.ok(employee));

        EmployeeWithTariffsDto result = restClientUbs.getEmployeeByEmail(email);

        assertNotNull(result);
    }

    @Test
    void testGetTariffIdByLocationId() {
        Long locationId = 1L;
        List<Long> expectedTariffId = List.of(100L);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(restTemplate.exchange(
            greenCityUbsServerAddress + "/ubs/tariffs/" + locationId,
            HttpMethod.GET,
            entity,
                new ParameterizedTypeReference<List<Long>>(){})).thenReturn(ResponseEntity.ok(expectedTariffId));

        List<Long> actualTariffId = restClientUbs.getTariffIdByLocationId(locationId);
        assertEquals(expectedTariffId, actualTariffId);
    }

    @Test
    void testGetEmployeesByTariffIdWithChat() {
        Long tariffId = 1L;
        List<EmployeeWithTariffsDto> expectedEmployees = new ArrayList<>();

        HttpEntity<String> entity = new HttpEntity<>(headers);
        when(restTemplate.exchange(greenCityUbsServerAddress + "/admin/ubs-employee/get-employees/" + tariffId,
            HttpMethod.GET, entity, new ParameterizedTypeReference<List<EmployeeWithTariffsDto>>() {
            }))
                .thenReturn(ResponseEntity.ok(expectedEmployees));
        List<EmployeeWithTariffsDto> actualEmployees = restClientUbs.getEmployeesByTariffIdWithChat(tariffId);
        assertEquals(expectedEmployees, actualEmployees);
    }
}