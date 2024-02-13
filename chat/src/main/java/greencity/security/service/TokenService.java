package greencity.security.service;

import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
    /**
     * Method that pass the token to cookies.
     *
     * @param accessToken {@link String}
     * @param response    {@link javax.servlet.http.HttpServletResponse}
     */
    void passTokenToCookies(String accessToken, HttpServletResponse response);
}
