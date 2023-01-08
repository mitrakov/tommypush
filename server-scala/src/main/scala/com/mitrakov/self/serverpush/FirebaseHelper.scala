package com.mitrakov.self.serverpush

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.google.firebase.messaging.{ApnsConfig, Aps, FirebaseMessaging, Message, Notification}
import com.typesafe.scalalogging.StrictLogging
import java.io.FileInputStream

class FirebaseHelper(secretConfig: String) extends StrictLogging {
  type MsgId = String

  val projectId = "tommypush-405b7"
  val credentials: GoogleCredentials = GoogleCredentials.fromStream(new FileInputStream(secretConfig))
  val options: FirebaseOptions = FirebaseOptions.builder().setCredentials(credentials).setProjectId(projectId).build()
  val app: FirebaseApp = FirebaseApp.initializeApp(options)

  def sendMessage(recipientFcmToken: String, title: String, body: String): MsgId = {
    logger.info("Sending message {}: {}", title, body)
    val notification = Notification.builder().setTitle(title).setBody(body).build()
    val apnsConfig = ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build()
    val msg = Message.builder().setNotification(notification).setApnsConfig(apnsConfig).setToken(recipientFcmToken).build()

    FirebaseMessaging.getInstance(app).send(msg)
  }
}
