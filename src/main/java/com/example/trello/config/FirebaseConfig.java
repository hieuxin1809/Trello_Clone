// src/main/java/com/example/trello/config/FirebaseConfig.java
package com.example.trello.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    // Khởi tạo Firebase App khi ứng dụng khởi động
    @PostConstruct
    public void initialize() throws IOException {
        try {
            // Đọc file Service Account JSON từ thư mục resources
            ClassPathResource resource = new ClassPathResource("firebase-admin-sdk.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    // Thêm databaseUrl nếu bạn dùng Firebase Realtime Database
                    // .setDatabaseUrl("https://<YOUR_PROJECT_ID>.firebaseio.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("Firebase Admin SDK initialized successfully.");

        } catch (IOException e) {
            System.err.println("Error initializing Firebase: " + e.getMessage());
            throw e;
        }
    }
}