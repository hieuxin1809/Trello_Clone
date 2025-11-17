    package com.example.trello.service;

    import com.example.trello.dto.request.VerifyTokenRequest;
    import com.example.trello.dto.response.VerifyTokenResponse;
    import com.example.trello.exception.AppException;
    import com.example.trello.exception.ErrorCode;
    import com.example.trello.model.User;
    import com.example.trello.repository.RedisTokenRepository;
    import com.nimbusds.jose.*;
    import com.nimbusds.jose.crypto.MACSigner;
    import com.nimbusds.jose.crypto.MACVerifier;
    import com.nimbusds.jwt.JWTClaimsSet;
    import com.nimbusds.jwt.SignedJWT;
    import lombok.AccessLevel;
    import lombok.RequiredArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import lombok.experimental.NonFinal;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.util.CollectionUtils;

    import java.text.ParseException;
    import java.time.Instant;
    import java.time.temporal.ChronoUnit;
    import java.util.*;

    @Service
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Slf4j
    public class JwtService {
        @NonFinal
        @Value("${spring.jwt.signerKey}")
        protected String SECRET_KEY;
        RedisTokenRepository redisTokenRepository;
        public String generateAccessToken(User user) {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer("Trello clone") // thuong se la domain
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope" , buildScope(user))
                    .build();
            Payload payload = new Payload(jwtClaimsSet.toJSONObject());

            JWSObject jwsObject = new JWSObject(header, payload);
            try {
                jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
                return jwsObject.serialize();
            } catch (JOSEException e) {
                log.error("cannot create token",e);
                throw new RuntimeException(e);
            }
        }
        public String generateRefreshToken(User user) {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer("Trello") // thuong se la domain
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(14, ChronoUnit.DAYS)))
                    .jwtID(UUID.randomUUID().toString())
                    .build();
            Payload payload = new Payload(jwtClaimsSet.toJSONObject());

            JWSObject jwsObject = new JWSObject(header, payload);
            try {
                jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
                return jwsObject.serialize();
            } catch (JOSEException e) {
                log.error("cannot create token",e);
                throw new RuntimeException(e);
            }
        }
        public VerifyTokenResponse verifyToken(VerifyTokenRequest request) {
            try {
                String accessToken = request.getToken();
                SignedJWT signedJWT = verifyToken(accessToken, true); // Verify đầy đủ

                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                String email = claims.getSubject();
                Date expiredTime = claims.getExpirationTime();
                String scopes = (String) claims.getClaim("scope");
                List<String> scopesList = scopes != null ? Arrays.asList(scopes.split(" ")) : Collections.emptyList();

                return VerifyTokenResponse.builder()
                        .isValid(true)
                        .email(email)
                        .expiredTime(expiredTime)
                        .scopes(scopesList)
                        .build();

            } catch (Exception e) {
                log.error("Token verification failed: {}", e.getMessage());
                return VerifyTokenResponse.builder()
                        .isValid(false)
                        .build();
            }
        }
        public SignedJWT verifyToken(String token, boolean checkBlacklist) throws JOSEException, ParseException {
            JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean verified = signedJWT.verify(verifier);

            if(!(verified && expiration.after(new Date()))){
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            if(checkBlacklist) {
                String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
                if(redisTokenRepository.existsById(jwtId)){
                    throw new AppException(ErrorCode.UNAUTHENTICATED);
                }
            }
            return signedJWT;
        }
        private String buildScope(User user) { // Dùng Entity User MongoDB mới
            // Giả định User Entity có phương thức getRole() trả về Enum/String
            if (user.getRole() == null) {
                return "";
            }
            return "ROLE_" + user.getRole().toString().toUpperCase();
        }
//        public JwtInfo parseToken(String token) throws ParseException {
//            SignedJWT signedJWT = SignedJWT.parse(token);
//            String jwtID = signedJWT.getJWTClaimsSet().getJWTID();
//            String email = signedJWT.getJWTClaimsSet().getSubject();
//            Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
//            Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//            return JwtInfo.builder()
//                    .jwtId(jwtID)
//                    .email(email)
//                    .issueTime(issueTime)
//                    .expiredTime(expiredTime)
//                    .build();
//        }
//        public boolean isTokenInvalid(String jwtId) {
//            return redisTokenRepository.findById(jwtId).isPresent();
//        }
//        public boolean verifyTokenSignature(SignedJWT signedJWT) throws JOSEException {
//            MACVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
//            return signedJWT.verify(verifier);
//        }

    }
