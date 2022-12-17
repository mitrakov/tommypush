package com.mitrakov.self.serverpush

import com.typesafe.scalalogging.LazyLogging
import sttp.client3.SttpApi
import java.time.LocalDate

object Main extends App with LazyLogging with SttpApi {
  if (args.length != 2) {
    Console.err.println("Usage:   java -jar tommypush.jar <path/to/firebase.json> <usd-rub-rate>\nExample: java -jar tommypush.jar /home/user1/firebase-adminsdk.json 76.2")
    System.exit(1)
  }

  val firebaseConfPath = args.head
  val desiredRate = args(1).toDouble

  // iPhone7
  val fcmToken1 = "cVXnVdTcM00lnKcKq4zrBn:APA91bFBMJQ87ryzNipCIVwlXOpwNl-3RWjOTw1Ei3yAFL6q3wr7bkVzRmXMeFYhtFYhEKlfogztMKTTRK4sVBLkLpCGh0NOCicbwqUjF2hJ-shta-lspkZBuTU5MWS6R2-cdBbmYvM1"
  // Samsung Galaxy S7
  val fcmToken2 = "fKloxEYmR7CinffPjQgUn2:APA91bE0-dyq0RRj9k1SFol8kO6xX39QnhQh2sHzZNTxRpZ_0cLlxhymlwA9TEKuIKOOLjG2bjPHjEv7iOA7fgWtB1sn1JX8oCev6j3EMHpmKfUZXybECpCUtUmqTSzWrpP-Yz6Awn2j"

  val firebase = new FirebaseHelper(firebaseConfPath)

  new Thread(new UsdChecker(desiredRate, firebase, fcmToken2)).start()
  new Thread(new AviasalesChecker(LocalDate.of(2022, 12, 31), 20000, firebase, fcmToken2)).start()
}
