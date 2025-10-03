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

/* bash example:
#!/bin/bash

fcm1="fHWRTKGHjUnvqSlrBdeqQO:APA91bG0tH-fcy44_OBirgoIt04s4s58k-9gEJPFg9vglCoux9nRdgH7SYMmxUD6HWrTgakeFZK3J6OeZIf2Qs8aaxDUpGRlh9zwBVwWU9Lkg9U5c3MzCYzkrAKf2vRSzr3ClqGPWLbU"

JAR=/Users/director/software/tommypush.jar
CONFIG=/Users/director/Yandex.Disk.localized/all/configs/firebase/tommypush-firebase.json
TITLE="Tommypush"
TEXT="Buenas días, Tommy"

java -jar $JAR $CONFIG "$TITLE" "$TEXT" "$fcm1"
*/
