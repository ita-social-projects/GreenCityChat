package greencity.security.filters;

import greencity.client.RestClient;
import greencity.dto.UserVO;
import greencity.jwt.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessTokenAuthenticationFilterTest {
    private PrintStream systemOut;
    private ByteArrayOutputStream systemOutContent;
    private UserVO userVO;

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain chain;
    @Mock
    JwtTool jwtTool;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    RestClient restClient;

    @InjectMocks
    private AccessTokenAuthenticationFilter filter;

    @BeforeEach
    public void setUp() {
        userVO = UserVO.builder()
            .id(1L)
            .email("test@gmail.com")
            .build();

        systemOut = System.out;
        systemOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(systemOutContent));
    }

    @Test
    void doFilterInternal() throws IOException, ServletException {
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn("TestAccessToken");
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@gmail.com", null));
        when(restClient.findNotDeactivatedByEmail("test@gmail.com")).thenReturn(Optional.of(userVO));

        doNothing().when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);
        verify(authenticationManager).authenticate(any());
        verify(chain).doFilter(request, response);

    }

    @Test
    void doFilterInternalTokenHasExpired() throws IOException, ServletException {
        String token = "TestAccessToken";
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(token);
        when(authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(token, null)))
                .thenThrow(ExpiredJwtException.class);

        filter.doFilterInternal(request, response, chain);
        assertTrue(systemOutContent.toString().contains("Token has expired: "));

    }

    @Test
    void doFilterInternalAccessDenied() throws IOException, ServletException {
        String token = "TestAccessToken";
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(token);
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@gmail.com", null));
        when(restClient.findNotDeactivatedByEmail("test@gmail.com")).thenThrow(RuntimeException.class);

        filter.doFilterInternal(request, response, chain);
        assertTrue(systemOutContent.toString().contains("denied"));
    }
}