package greencity.client;

import greencity.dto.GetEmployeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
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

    public List<GetEmployeeDto> getEmployeesByTariffId(Long tariffId) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        ResponseEntity<List<GetEmployeeDto>> response = restTemplate.exchange(
                greenCityUbsServerAddress + "/admin/ubs-employee/get-employees/" + tariffId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    private HttpHeaders setHeader() {
        String accessToken = httpServletRequest.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        return headers;
    }
}