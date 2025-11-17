package com.example.trello.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // (2) Đây là endpoint (cửa) để client kết nối WebSocket
        registry.addEndpoint("/ws") // URL sẽ là: ws://localhost:8080/ws
                .setAllowedOriginPatterns("*") // Cho phép mọi nguồn gốc (CORS)
                .withSockJS(); // (3) Bật SockJS để dự phòng khi WebSocket không được hỗ trợ
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // (4) Đích đến cho các tin nhắn TỪ SERVER GỬI VỀ CLIENT
        // Client sẽ subscribe (đăng ký) vào các kênh bắt đầu bằng "/topic"
        // Ví dụ: /topic/board/123
        registry.enableSimpleBroker("/topic");

        // (5) Đích đến cho các tin nhắn TỪ CLIENT GỬI LÊN SERVER
        // Server sẽ xử lý các tin nhắn có đích đến bắt đầu bằng "/app"
        // Ví dụ: /app/card/move (sẽ được map vào @MessageMapping("/card/move"))
        registry.setApplicationDestinationPrefixes("/app");
    }
}
