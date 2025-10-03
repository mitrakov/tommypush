package com.mitrakoff.self.tommypush;

// mvn package
public class Main {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage:   java -jar tommypush.jar  FirebaseConfig  Title  Message  FCM_Tokens...");
            System.err.println("Example: java -jar tommypush.jar /etc/secrets/firebase.json \"Some Title\" \"Hello world\" \"fHWRTKGHjUnvqSlrBdeqQO\"");
            System.exit(1);
        }

        int i = 0;
        final String firebaseConfPath = args[i++];
        final String title = args[i++];
        final String body = args[i++];

        try (final FirebaseHelper firebase = new FirebaseHelper(firebaseConfPath)) {
            while (i < args.length) {
                final String fcmToken = args[i++];
                firebase.sendMessage(fcmToken, title, body);
            }
        }
    }
}
