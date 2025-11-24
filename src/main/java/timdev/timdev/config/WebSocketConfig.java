package timdev.timdev.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        // config.enableSimpleBroker("/topic");  // Prefix for broadcasting messages
        config.setApplicationDestinationPrefixes("/app", "/user");  // Prefix for client-to-server communication
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // This is the WebSocket endpoint
                .setAllowedOriginPatterns("*")
                // .setAllowedOrigins("*")  // Allow frontend origin (localhost:63342)
                .withSockJS();  // Enable SockJS for fallback support
    }
}