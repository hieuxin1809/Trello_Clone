package com.example.trello.config;

import com.example.trello.service.UserDetailServiceCustomizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // PUBLIC ENDPOINTS cho Trello: Đăng ký, Đăng nhập, Introspcet, Refresh
    // (2) ĐỊNH NGHĨA LẠI CÁC ENDPOINT PUBLIC
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/log-in",
            "/auth/register",
            "/auth/refresh",

            // Endpoint cho Firebase Cloud Messaging (nếu có)
            "/fcm/**",

            // Endpoint để test WebSocket
            "/test-websocket.html",
            "/ws/**",

            "/test-firebase.html",
            "/firebase-messaging-sw.js",
            "/favicon.ico"


    };

    private final CustomJwtDecoder jwtDecoder;
    private final UserDetailServiceCustomizer userDetailsServiceCustomizer;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // (3) BẬT CORS LÊN TRƯỚC
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.authorizeHttpRequests(auth -> auth
                // (4) CHỈ CHO PHÉP PUBLIC ENDPOINTS
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                // (5) TẤT CẢ CÁC REQUEST CÒN LẠI PHẢI ĐƯỢC XÁC THỰC (CẦN TOKEN)
                .anyRequest().authenticated()
        );

        // Vẫn giữ cấu hình OAuth2 Resource Server
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        // Vô hiệu hóa CSRF
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
    @Bean
    JwtAuthenticationConverter jwtConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Giữ nguyên prefix rỗng để ROLE_USER được map thành ROLE_USER
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsServiceCustomizer);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(authenticationProvider);
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép frontend (ví dụ: localhost:3000) gọi API
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true); // Cho phép gửi cookie/token
        config.setMaxAge(3600L); // Thời gian cache cấu hình CORS

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Áp dụng cho TẤT CẢ các đường dẫn API
        return source;
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder(10);}
}
