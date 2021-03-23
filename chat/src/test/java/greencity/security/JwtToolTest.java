package greencity.security;

import greencity.jwt.JwtTool;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class JwtToolTest {
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtTool jwtTool;

    @Test
    void getTokenFromHttpServletRequest() {
        final String expectedToken = "An AccessToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expectedToken);
        String actualToken = jwtTool.getTokenFromHttpServletRequest(request);
        assertEquals(expectedToken, actualToken);
    }
}
