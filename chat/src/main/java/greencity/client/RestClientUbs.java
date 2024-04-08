package greencity.client;

import greencity.dto.GetEmployeeDto;
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

    private HttpHeaders setHeader() {
        String accessToken = httpServletRequest.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        return headers;
    }
}