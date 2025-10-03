package com.mitrakoff.self.tommypush;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import com.google.firebase.messaging.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;

public class FirebaseHelper implements AutoCloseable {
    public final FirebaseApp app;

    public FirebaseHelper(String secretConfig) {
        try {
            final GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(Paths.get(secretConfig)));
            final FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            app = FirebaseApp.initializeApp(options);
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    /** @return messageID from Firebase Cloud Messaging */
    public String sendMessage(String recipientFcmToken, String title, String body) {
        System.out.printf("%s: Sending message %s: %s\n", LocalDateTime.now(), title, body);
        final Notification notification = Notification.builder().setTitle(title).setBody(body).build();
        final ApnsConfig apnsConfig = ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build();
        final Message message = Message.builder().setNotification(notification).setApnsConfig(apnsConfig).setToken(recipientFcmToken).build();

        try {
            return FirebaseMessaging.getInstance(app).send(message);
        } catch (FirebaseMessagingException e) {e.printStackTrace(); return "";}
    }

    @Override
    public void close() { app.delete(); }
}
