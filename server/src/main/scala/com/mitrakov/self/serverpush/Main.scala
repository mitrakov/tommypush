package com.mitrakov.self.serverpush

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.{ApnsConfig, Aps, FirebaseMessaging, Message, Notification}
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.typesafe.scalalogging.LazyLogging
import io.burt.jmespath.jackson.JacksonRuntime
import sttp.client3.{HttpClientSyncBackend, SttpApi}
import java.io.FileInputStream
import java.time.{Duration, LocalDateTime}

object Main extends App with LazyLogging with SttpApi {
  type MsgId = String
  if (args.length != 2) {
    Console.err.println("Usage:   java -jar tommypush.jar <path/to/firebase.json> <usd-rub-rate>\nExample: java -jar tommypush.jar /home/user1/firebase-adminsdk.json 76.2")
    System.exit(1)
  }

  val firebaseConfPath = args.head
  val desiredRate = args(1).toDouble
  val projectId = "tommypush-405b7"
  val fcmToken = "cVXnVdTcM00lnKcKq4zrBn:APA91bFBMJQ87ryzNipCIVwlXOpwNl-3RWjOTw1Ei3yAFL6q3wr7bkVzRmXMeFYhtFYhEKlfogztMKTTRK4sVBLkLpCGh0NOCicbwqUjF2hJ-shta-lspkZBuTU5MWS6R2-cdBbmYvM1"
  var lastSentMsgTime = LocalDateTime.parse("2000-01-01T00:00:00")

  lazy val sttp = HttpClientSyncBackend()
  lazy val jqRuntime = new JacksonRuntime()
  lazy val mapper = new ObjectMapper()
  lazy val firebase = initFirebase(firebaseConfPath)

  while (true) {
    val minutes = Duration.between(lastSentMsgTime, LocalDateTime.now).toMinutes
    if (minutes >= 120) {
      makeRequest() match {
        case Right(json) =>
          val realRate = parseDouble(json, jmesPath = "marketdata.data[0][8]")
          if (realRate >= desiredRate) {

            sendMessage(firebase, "Tommy Push Notification", s"$realRate ≥ $desiredRate!")
            lastSentMsgTime = LocalDateTime.now()
          }
        case Left(error) => logger.error(error)
      }
    }
    Thread.sleep(5000L)
  }

  def makeRequest(): Either[String, String] = {
    val response = basicRequest.get(uri"https://iss.moex.com/iss/engines/currency/markets/selt/securities.jsonp?iss.meta=off&iss.only=marketdata&securities=CETS:USD000UTSTOM").send(sttp)
    val result = response.body
    logger.debug(result.toString)
    result
  }

  def parseDouble(json: String, jmesPath: String): Double = {
    val jq = jqRuntime.compile(jmesPath)
    val node = mapper.readTree(json)
    val result = jq.search(node)
    logger.info(s"Result is $result")
    result.asDouble()
  }

  def initFirebase(secretConfig: String): FirebaseApp = {
    val credentials = GoogleCredentials.fromStream(new FileInputStream(secretConfig))
    val options = FirebaseOptions.builder().setCredentials(credentials).setProjectId(projectId).build()

    FirebaseApp.initializeApp(options)
  }

  def sendMessage(app: FirebaseApp, title: String, body: String): MsgId = {
    logger.info("Sending message {}: {}", title, body)
    val notification = Notification.builder().setTitle(title).setBody(body).build()
    val apnsConfig = ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build()
    val msg = Message.builder().setNotification(notification).setApnsConfig(apnsConfig).setToken(fcmToken).build()

    FirebaseMessaging.getInstance(app).send(msg)
  }
}
