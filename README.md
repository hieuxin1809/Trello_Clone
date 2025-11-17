# Hệ thống Quản lý Dự án Cộng tác

Một ứng dụng quản lý dự án (lấy cảm hứng từ Trello) được xây dựng với Spring Boot và MongoDB, hỗ trợ cộng tác thời gian thực.

## Tính năng chính

* **Quản lý dự án:** Tạo và quản lý bảng (boards), danh sách (lists), và thẻ (cards).
* **Cộng tác thời gian thực:** Tự động cập nhật các thay đổi (như kéo-thả thẻ, bình luận mới) đến mọi người dùng trong bảng qua WebSocket (STOMP).
* **Thông báo:** Gửi thông báo đẩy (Firebase FCM) và thông báo trong ứng dụng khi có cập nhật quan trọng (như @mention hoặc được gán thẻ).
* **Bảo mật:** Đăng nhập bằng JWT (Access/Refresh Token), phân quyền vai trò trên board (Owner, Manager, Member).
* **Tính năng thẻ:** Hỗ trợ kéo-thả, gán thành viên, bình luận, và tải tệp đính kèm (Cloudinary).
* **Nhật ký hoạt động:** Ghi lại các thay đổi quan trọng xảy ra trong bảng.

## Công nghệ sử dụng

Spring Boot, Spring Security, Spring Data MongoDB, WebSocket (STOMP), Redis, Firebase (FCM), và Cloudinary.
