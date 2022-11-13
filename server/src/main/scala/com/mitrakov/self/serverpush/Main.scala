package com.mitrakov.self.serverpush

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.{FirebaseMessaging, Message, Notification}
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
  val not = Notification.builder().setTitle("Title bro").setBody("Body, man").build()
  val msg = Message
    .builder()
    .setNotification(not)
    //.setToken("C93BCD6D8E92C0227B2237B58D7D3E2575F1F5DD533DBB83A9E91E6AB10E5E82")
    .setToken("ff1BAIO5MkTxgl1Y4Ho2_V:APA91bGx-B1_g93mxxXT5mrzzOYPnVR9_Pgp4eVXwzJxqJaHXtW74Fb1C7GyeOATt-443KTiJ0QGaqI1Si_H2zMwzelAdwd_4vIE92Me7jVArQ04GE3IqhUnctEocWAtJuiXqd0h4kTF")
    .build()
  val msgID = FirebaseMessaging.getInstance(app).send(msg)
  println(msgID)
}
