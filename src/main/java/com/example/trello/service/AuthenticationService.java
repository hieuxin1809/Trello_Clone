package com.example.trello.service;

import com.example.trello.dto.request.LoginRequest;
import com.example.trello.dto.request.LogoutRequest;
import com.example.trello.dto.request.RefreshRequest;
import com.example.trello.dto.response.LoginResponse;
import com.example.trello.dto.response.RefreshTokenResponse;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.model.RedisToken;
import com.example.trello.model.User;
import com.example.trello.repository.RedisTokenRepository;
import com.example.trello.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    JwtService jwtService;
    RedisTokenRepository redisTokenRepository;
    AuthenticationManager authenticationManager;
    public LoginResponse login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        User userEntity = (User) authenticate.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userEntity);
        String refreshToken = jwtService.generateRefreshToken(userEntity);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    public void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException {
        String token = logoutRequest.getToken().replace("Bearer ", "");

        // Verify signature + expiration, không check blacklist
        SignedJWT signedJWT = jwtService.verifyToken(token, false);

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Blacklist token
        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtId)
                .expiredTime(expiredTime.getTime() - new Date().getTime())
                .build();
        redisTokenRepository.save(redisToken);
    }
    public RefreshTokenResponse refresh(RefreshRequest request) throws ParseException, JOSEException {
        String refreshToken = request.getToken();

        // Verify signature + expiration, khong check blacklist
        SignedJWT signedJWT = jwtService.verifyToken(refreshToken, false);

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String jwtId = claims.getJWTID();
        Date expiredTime = claims.getExpirationTime();
        String email = claims.getSubject();

        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtId)
                .expiredTime(expiredTime.getTime() - new Date().getTime())
                .build();
        redisTokenRepository.save(redisToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return RefreshTokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}
