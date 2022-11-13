package com.mitrakov.self.serverpush

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging._
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import java.io.FileInputStream

object Main extends App {
  val path = "/Users/macbook1/Yandex.Disk.localized/mine/config/firebase/tommypush-firebase-adminsdk.json"
  val options = FirebaseOptions
    .builder()
    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(path)))
    .setProjectId("tommypush-405b7")
    .build()
  val app = FirebaseApp.initializeApp(options)

  val msg = Message
    .builder()
    .setNotification(Notification.builder().setTitle("Tommy Push Notification").setBody("Hello, man!").build())
    .setApnsConfig(ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build())
    .setToken("cVXnVdTcM00lnKcKq4zrBn:APA91bFBMJQ87ryzNipCIVwlXOpwNl-3RWjOTw1Ei3yAFL6q3wr7bkVzRmXMeFYhtFYhEKlfogztMKTTRK4sVBLkLpCGh0NOCicbwqUjF2hJ-shta-lspkZBuTU5MWS6R2-cdBbmYvM1")
    .build()
  val msgId = FirebaseMessaging.getInstance(app).send(msg)
  println(msgId)
}
