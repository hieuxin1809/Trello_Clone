// Import SDK (phiên bản này phải khớp với file html)
importScripts("https://www.gstatic.com/firebasejs/8.10.0/firebase-app.js");
importScripts("https://www.gstatic.com/firebasejs/8.10.0/firebase-messaging.js");

// DÁN LẠI CÁI firebaseConfig CỦA BẠN VÀO ĐÂY
const firebaseConfig = {
    apiKey: "AIzaSyDeUo9Bch4qAZ4zD3JfMRnS5B3VoqP9rcs",
    authDomain: "trello-clone-f8c96.firebaseapp.com",
    projectId: "trello-clone-f8c96",
    storageBucket: "trello-clone-f8c96.firebasestorage.app",
    messagingSenderId: "195173265295",
    appId: "1:195173265295:web:3df3e16b614b8283d925fa",
    measurementId: "G-698ZV0TBFX"
};

// Khởi tạo
firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// (Không bắt buộc, nhưng nên có)
// Thêm để xử lý thông báo khi trang web đang chạy ngầm
messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);
    // Tùy chỉnh thông báo ở đây
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/firebase-logo.png' // Bạn có thể thêm 1 cái icon
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});