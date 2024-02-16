package greencity.config;

import static greencity.constant.AppConstant.*;
import static greencity.constant.AppConstant.UBS_EMPLOYEE;

import greencity.client.RestClient;
import greencity.jwt.JwtTool;
import greencity.security.providers.JwtAuthenticationProvider;
import java.util.Arrays;
import java.util.Collections;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Config for security.
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication
public class SecurityConfig {
    private final JwtTool jwtTool;
    private final RestClient restClient;
    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * Constructor.
     */
    @Autowired
    public SecurityConfig(JwtTool jwtTool, RestClient restClient, AuthenticationConfiguration authenticationConfiguration) {
        this.jwtTool = jwtTool;
        this.restClient = restClient;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    /**
     * Bean {@link PasswordEncoder} that uses in coding password.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Method for configure security.
     *
     * @param http {@link HttpSecurity}
     */
    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable).
                csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(
                        new greencity.security.filters.AccessTokenAuthenticationFilter(jwtTool, authenticationManager(),
                                restClient),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, resp, exc) -> resp.sendError(SC_UNAUTHORIZED, "Authorize first."))
                        .accessDeniedHandler((req, resp, exc) -> resp.sendError(SC_FORBIDDEN, "You don't have authorities.")))
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/css/**",
                                "/img/**",
                                "/socket",
                                "/socket/**",
                                "/socket/**/**",
                                "/socket/info")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/chat",
                                "/chat/**",
                                "/chat/create-chatRoom",
                                "/chat/messages/{room_id}",
                                "/chat/room/{room_id}",
                                "/chat/last/message",
                                "/chat/exist/{fistUserId}/{secondUserId}",
                                "/chat/rooms",
                                "/chat/user",
                                "/chat/user/{id}",
                                "/chat/users/**")
                        .hasAnyRole(USER, ADMIN, MODERATOR, UBS_EMPLOYEE)
                        .requestMatchers(HttpMethod.POST,
                                "/chat/create-chatRoom",
                                "/chat/sent-message/{userId}/{roomId}")
                        .hasAnyRole(USER, ADMIN, MODERATOR, UBS_EMPLOYEE)
                        .anyRequest().hasAnyRole(ADMIN));
        return http.build();
    }

    /**
     * Method for configure matchers that will be ignored in security.
     *
     * @return {@link WebSecurityCustomizer}
     */
    @Bean
     public WebSecurityCustomizer webSecurityCustomizer() {
         return web -> {
             web.ignoring().requestMatchers("/v2/api-docs/**");
             web.ignoring().requestMatchers("/swagger.json");
             web.ignoring().requestMatchers("/swagger-ui.html");
             web.ignoring().requestMatchers("/swagger-resources/**");
             web.ignoring().requestMatchers("/webjars/**");
         };
     }

    /**
     * Method for configure type of authentication provider.
     *
     * @param auth {@link AuthenticationManagerBuilder}
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new JwtAuthenticationProvider(jwtTool));
    }

    /**
     * Provides AuthenticationManager.
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Bean {@link CorsConfigurationSource} that uses for CORS setup.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
        configuration.setAllowedHeaders(
            Arrays.asList(
                "X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
