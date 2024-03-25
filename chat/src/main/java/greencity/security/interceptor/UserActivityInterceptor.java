package greencity.security.interceptor;

import greencity.client.RestClient;
import lombok.AllArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

@AllArgsConstructor
public class UserActivityInterceptor implements HandlerInterceptor {
    private final RestClient restClient;
}
