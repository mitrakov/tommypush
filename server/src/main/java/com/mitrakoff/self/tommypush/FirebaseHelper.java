package com.mitrakoff.self.tommypush;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import com.google.firebase.messaging.*;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseHelper {
    public final FirebaseApp app;

    public FirebaseHelper(String secretConfig) throws IOException {
        final String projectId = "tommypush-405b7";
        final GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(secretConfig));
        final FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).setProjectId(projectId).build();
        app = FirebaseApp.initializeApp(options);
    }

    public String sendMessage(String recipientFcmToken, String title, String body) throws FirebaseMessagingException {
        System.out.printf("Sending message %s: %s\n", title, body);
        final Notification notification = Notification.builder().setTitle(title).setBody(body).build();
        final ApnsConfig apnsConfig = ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build();
        final Message message = Message.builder().setNotification(notification).setApnsConfig(apnsConfig).setToken(recipientFcmToken).build();

        return FirebaseMessaging.getInstance(app).send(message);
    }
}
