package com.example.trello.controller;

import com.example.trello.model.NotificationToken;
import com.example.trello.repository.NotificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {
    private final NotificationTokenRepository tokenRepository;

    @PostMapping("/register")
    public String registerToken(@RequestParam String userId, @RequestParam String token) {
        tokenRepository.findByUserId(userId).ifPresentOrElse(
                existing -> {
                    existing.setToken(token);
                    tokenRepository.save(existing);
                },
                () -> tokenRepository.save(NotificationToken.builder()
                        .userId(userId)
                        .token(token)
                        .build())
        );
        return "✅ Token saved successfully";
    }
}
