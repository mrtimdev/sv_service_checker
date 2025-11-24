package timdev.timdev.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class PusherBeamsService {


        @Value("${pusher.beams.instance-id}")
        private String instanceId;

        @Value("${pusher.beams.secret-key}")
        private String secretKey;

        private final RestTemplate restTemplate = new RestTemplate();

        public void sendToInterest(String interest, String title, String body) {
                String url = "https://" + instanceId + ".pushnotifications.pusher.com/publish_api/v1/instances/"
                                + instanceId + "/publishes/interests";

                Map<String, Object> payload = Map.of(
                        "interests", List.of(interest),
                        "web", Map.of(
                                "notification", Map.of(
                                        "title", title,
                                        "body", body,
                                        "deep_link", "https://chatgpt.com/c/691d905b-6fbc-8322-bbca-0a0cb82283fb"
                                )
                        )
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(secretKey);  // Use your secret key here

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                restTemplate.postForEntity(url, request, String.class);
        }



        public void sendToUser(String userId, String title, String body) {

                String url = "https://" + instanceId + ".pushnotifications.pusher.com" +
                        "/publish_api/v1/instances/" + instanceId + "/publishes/users";

                Map<String, Object> payload = Map.of(
                        "users", List.of(userId),
                        "interests", List.of("hello"),
                        "web", Map.of(
                                "notification", Map.of(
                                        "title", title,
                                        "body", body,
                                        "deep_link", "https://www.pusher.com"
                                )
                        )
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // ✅ Use Bearer token instead of Basic Auth
                headers.setBearerAuth(secretKey);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                restTemplate.postForEntity(url, request, String.class);
        }

}