package greencity.service.client;

import greencity.client.RestClient;
import greencity.dto.UserVO;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestClientTest {
    @InjectMocks
    private RestClient restClient;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpServletRequest request;
    @Value("localhost:1111")
    private String greenCityUserServerAddress;
    @Mock
    private java.lang.Object Object;

    @Test
    void findIdByEmailTest() {
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestClient restClient = new RestClient(restTemplate, request);
        when(request.getHeader("Authorization")).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityUserServerAddress
            + "/user/findIdByEmail" + "?email=taras@gmail.com", HttpMethod.GET, entity, Long.class)).thenReturn(
                ResponseEntity.ok(1L));

        assertEquals(1L, restClient.findIdByEmail("taras@gmail.com"));
    }

    @Test
    void updateUserLastActivityTimeTest() {
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestClient restClient = new RestClient(restTemplate, request);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        Date date = new Date();
        when(request.getHeader("Authorization")).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityUserServerAddress + "/user/"
            + 1L + "/updateUserLastActivityTime/" + dateFormat.format(date),
            HttpMethod.PUT, entity, Object.class)).thenReturn(ResponseEntity.ok(Object));
        restClient.updateUserLastActivityTime(1L, date);
        verify(restTemplate).exchange(greenCityUserServerAddress
            + "/user/1/updateUserLastActivityTime/" + dateFormat.format(date),
            HttpMethod.PUT, entity, Object.class);
    }

    @Test
    void findNotDeactivatedByEmail() {
        String email = "test@gmail.com";
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserVO userVO = new UserVO(1L, "test@gmail.com");
        when(request.getHeader("Authorization")).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityUserServerAddress
            + "/user/findNotDeactivatedByEmail" + "?email="
            + email, HttpMethod.GET, entity, UserVO.class)).thenReturn(ResponseEntity.ok(userVO));

        assertEquals(Optional.of(userVO), restClient.findNotDeactivatedByEmail(email));
    }
}
