package greencity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ChatServiceApplication {
    /**
     * Chat method of SpringBoot app.
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }

    /**
     * Bean to return RestTemplate.
     *
     * @return {@link RestTemplate}.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
