package greencity.client;

import greencity.dto.GetEmployeeDto;
import greencity.dto.LocationsDto;
import greencity.exception.exceptions.TariffNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
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
     * Retrieves a list of employees by their associated tariff ID.
     *
     * @param tariffId The ID of the tariff for which to retrieve employees.
     * @return A list of {@link GetEmployeeDto} objects representing the employees
     *         associated with the specified tariff.
     * @throws RestClientException if an error occurs during the REST API call.
     */
    public List<GetEmployeeDto> getEmployeesByTariffId(Long tariffId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        ResponseEntity<List<GetEmployeeDto>> response = restTemplate.exchange(
            greenCityUbsServerAddress + "/admin/ubs-employee/get-employees/" + tariffId,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<>() {
            });
        return response.getBody();
    }

    /**
     * Checks if a tariff exists by its ID.
     *
     * @param tariffInfoId The ID of the tariff to check.
     * @return {@code true} if the tariff exists, {@code false} otherwise.
     * @throws TariffNotFoundException if the tariff with the specified ID is not
     *                                 found.
     * @throws RuntimeException        if a server error occurs or an error occurs
     *                                 during the REST API call.
     */
    public boolean checkIfTariffExistsById(Long tariffInfoId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                greenCityUbsServerAddress + "/ubs/check-if-tariff-exists/" + tariffInfoId,
                HttpMethod.GET,
                entity,
                Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TariffNotFoundException("RestClient - Tariff not found with id: " + tariffInfoId);
        } catch (HttpServerErrorException.InternalServerError ex) {
            throw new RuntimeException("Server error occurred while retrieving tariff with id: " + tariffInfoId);
        } catch (RestClientException ex) {
            throw new RuntimeException("RestClient - Error occurred while retrieving tariff with id: " + tariffInfoId);
        }
    }

    /**
     * Retrieves a list of all locations from the UBS service.
     *
     * @return A list of {@link LocationsDto} objects representing all locations.
     * @throws RuntimeException if the locations are not found, if there's a failure
     *                          to retrieve locations (with a specific HTTP status
     *                          code), or if a server error occurs during the
     *                          process.
     */
    public List<LocationsDto> getAllLocations() {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        try {
            ResponseEntity<List<LocationsDto>> response = restTemplate.exchange(
                greenCityUbsServerAddress + "/ubs/locations",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                });

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Locations not found");
            } else {
                throw new RuntimeException("Failed to retrieve locations. Status code: " + response.getStatusCode());
            }
        } catch (HttpServerErrorException.InternalServerError ex) {
            throw new RuntimeException("Server error occurred while retrieving locations");
        } catch (RestClientException ex) {
            throw new RuntimeException("Error occurred while retrieving locations");
        }
    }

    /**
     * Retrieves the tariff ID associated with the given location ID from the UBS
     * service.
     * <p>
     * This method sends a GET request to the UBS service to retrieve the tariff ID
     * associated with the specified location ID. It handles different HTTP status
     * codes and throws appropriate exceptions in case of errors.
     * </p>
     *
     * @param locationId The ID of the location for which to retrieve the tariff ID.
     * @return The tariff ID associated with the specified location ID.
     * @throws TariffNotFoundException if the tariff is not found for the given
     *                                 location ID.
     * @throws RuntimeException        if a server error occurs during the process
     *                                 or if an error occurs while retrieving the
     *                                 tariff ID.
     */
    public Long getTariffIdByLocationId(Long locationId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        try {
            ResponseEntity<Long> response = restTemplate.exchange(
                greenCityUbsServerAddress + "/ubs/tariffs/" + locationId,
                HttpMethod.GET,
                entity,
                Long.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TariffNotFoundException("Tariff not found for location ID: " + locationId);
        } catch (HttpServerErrorException.InternalServerError ex) {
            throw new RuntimeException("Server error occurred while retrieving tariff ID");
        } catch (RestClientException ex) {
            throw new RuntimeException("Error occurred while retrieving tariff ID");
        }
    }

    private HttpHeaders setHeader() {
        String accessToken = httpServletRequest.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        return headers;
    }
}