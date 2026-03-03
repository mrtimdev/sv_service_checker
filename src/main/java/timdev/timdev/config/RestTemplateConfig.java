package timdev.timdev.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory((Supplier<ClientHttpRequestFactory>) HttpComponentsClientHttpRequestFactory::new)
                .setConnectTimeout(Duration.ofMillis(30000))
                .setReadTimeout(Duration.ofMillis(30000))
                .build();
    }

}
