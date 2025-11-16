package com.example.trello.controller;

import com.example.trello.dto.request.*;
import com.example.trello.dto.response.*;
import com.example.trello.service.AuthenticationService;
import com.example.trello.service.JwtService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    JwtService jwtService;
    @PostMapping("/log-in")
    ApiResponse<LoginResponse> authenticate(@RequestBody LoginRequest loginRequest) {
       return ApiResponse.<LoginResponse>builder()
               .data(authenticationService.login(loginRequest))
               .build();
    }
    @PostMapping("/register")
    ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.<RegisterResponse>builder()
                .data(authenticationService.register(request))
                .build();
    }
    @PostMapping("/verify-token")
    ApiResponse<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<VerifyTokenResponse>builder()
                .data(jwtService.verifyToken(request))
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestHeader("Authorization") LogoutRequest logoutRequest) throws ParseException, JOSEException {
        authenticationService.logout(logoutRequest);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PostMapping("/refresh")
    ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        return ApiResponse.<RefreshTokenResponse>builder()
                .data(authenticationService.refresh(request))
                .build();
    }
}