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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // PUBLIC ENDPOINTS cho Trello: Đăng ký, Đăng nhập, Introspcet, Refresh
    private final String[] PUBLIC_ENDPOINTS =
            {"/users","/auth/log-in","/auth/logout","/auth/introspect","/auth/refresh", "/fcm/register"}; // Đã sửa

    private final CustomJwtDecoder jwtDecoder;
    private final UserDetailServiceCustomizer userDetailsServiceCustomizer;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth

                // MỞ TẤT CẢ CÁC ENDPOINT AUTH VÀ CRUD CHO MỤC ĐÍCH TEST
                .requestMatchers("/user/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/boards/**").permitAll()
                .requestMatchers("/lists/**").permitAll()
                .requestMatchers("/cards/**").permitAll()
                .requestMatchers("/comments/**").permitAll()
                .requestMatchers("/labels/**").permitAll()
                .requestMatchers("/notifications/**").permitAll()
                .requestMatchers("/activities/**").permitAll()
                .requestMatchers("/fcm/**").permitAll() // Endpoint FCM

                // Các request khác (nếu có)
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
    public BCryptPasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder(10);}
}
