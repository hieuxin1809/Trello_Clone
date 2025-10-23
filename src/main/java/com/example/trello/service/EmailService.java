package com.example.trello.service;


import com.example.trello.model.User;
import com.example.trello.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    JavaMailSender mailSender;
    UserRepository userRepository;
    public void sendNotificationEmail(String userId, String subject, String text) {
        // 1. Lấy email của người nhận
        User user = userRepository.findById(userId)
                .orElse(null); // Hoặc ném AppException

        if (user == null || user.getEmail() == null) {
            System.err.println("Không tìm thấy người dùng hoặc email để gửi thông báo.");
            return;
        }

        // 2. Tạo nội dung email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        // Có thể thêm message.setFrom(your_configured_email);

        // 3. Gửi
        try {
            mailSender.send(message);
            System.out.println("✅ Email notification sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
        }
    }
}
