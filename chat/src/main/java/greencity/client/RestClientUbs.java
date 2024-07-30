package greencity.client;

import greencity.dto.EmployeeWithTariffsDto;
import greencity.dto.LocationsDto;
import greencity.exception.exceptions.TariffNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RestClientUbs {
    private final RestTemplate restTemplate;
    @Value("${greencityubs.server.address}")
    private String greenCityUbsServerAddress;
    private final HttpServletRequest httpServletRequest;

    /**
     * Checks if a tariff exists by its ID.
     *
     * @param tariffInfoId The ID of the tariff to check.
     * @return {@code true} if the tariff exists, {@code false} otherwise.
     */
    public boolean checkIfTariffExistsById(Long tariffInfoId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());

        return restTemplate.exchange(
            greenCityUbsServerAddress + "/ubs/check-if-tariff-exists/" + tariffInfoId,
            HttpMethod.GET,
            entity,
            Boolean.class).getBody();
    }

    /**
     * Retrieves a list of all locations from the UBS service.
     *
     * @return A list of {@link LocationsDto} objects representing all locations.
     */
    public List<LocationsDto> getAllLocations() {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());

        return restTemplate.exchange(
            greenCityUbsServerAddress + "/ubs/locations",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<LocationsDto>>() {
            }).getBody();
    }

    /**
     * Retrieves a list of all locations from the UBS service.
     *
     * @return A list of {@link LocationsDto} objects representing all locations.
     */
    public List<LocationsDto> getAllLocationsByCourierId(Long courierId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());

        return restTemplate.exchange(
                greenCityUbsServerAddress + "/ubs/locationsByCourier/" + courierId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<LocationsDto>>() {
                }).getBody();
    }

    /**
     * Retrieves the tariff ID associated with the given location ID from the UBS
     * service.
     *
     * @param locationId The ID of the location for which to retrieve the tariff ID.
     * @return The tariff ID associated with the specified location ID.
     */
    public Long getTariffIdByLocationId(Long locationId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
            greenCityUbsServerAddress + "/ubs/tariffs/" + locationId,
            HttpMethod.GET,
            entity,
            Long.class).getBody();
    }

    /**
     * Retrieves a list of employees with tariffs based on the specified tariff ID.
     *
     * @param tariffId The ID of the tariff to filter employees.
     * @return A list of {@code EmployeeWithTariffsDto} objects representing
     *         employees with tariffs.
     */
    public List<EmployeeWithTariffsDto> getEmployeesByTariffIdWithChat(Long tariffId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<EmployeeWithTariffsDto>> response = restTemplate.exchange(
            greenCityUbsServerAddress + "/admin/ubs-employee/get-employees/" + tariffId,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<>() {
            });
        return response.getBody();
    }

    /**
     * Retrieves an employee with tariffs by their email.* This method sends a GET
     * request to the UBS service to retrieve an employee with tariffs associated
     * with the specified email. It handles different HTTP status codes and throws
     * appropriate exceptions in case of errors.
     *
     * @param email The email of the employee to retrieve.
     * @return An {@code EmployeeWithTariffsDto} object representing the employee
     *         with tariffs.
     */
    public EmployeeWithTariffsDto getEmployeeByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());

        ResponseEntity<EmployeeWithTariffsDto> response = restTemplate.exchange(
            greenCityUbsServerAddress + "/admin/ubs-employee/" + email,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<>() {
            });
        return response.getBody();
    }

    private HttpHeaders setHeader() {
        String accessToken = httpServletRequest.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        return headers;
    }
}
