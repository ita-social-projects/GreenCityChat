package greencity.security.service.impl;

import greencity.exception.exceptions.BadRequestException;
import greencity.security.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    /**
     * {@inheritDoc}
     */
    @Override
    public void passTokenToCookies(String accessToken, HttpServletResponse response) {
        String checkToken = "eyJhbGciOiJIUzI1NiJ9";
        if (!accessToken.contains(checkToken)) {
            throw new BadRequestException("bad access token");
        }
        Cookie cookie = new Cookie("chatAccessToken", accessToken);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }
}
