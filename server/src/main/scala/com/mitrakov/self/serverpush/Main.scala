package com.mitrakov.self.serverpush

// build: "sbt assembly"
object Main extends App {
  if (args.length != 3) {
    Console.err.println("Usage:   java -jar tommypush.jar <path/to/firebase.json> <usd-rate> <aviasales-rate>\nExample: java -jar tommypush.jar /home/user1/firebase-adminsdk.json 76.2 10000")
    System.exit(1)
  }

  val firebaseConfPath = args.head
  val desiredUsdRate = args(1).toDouble
  val desiredAviasalesRate = args(2).toDouble

  // iPhone7
  val fcmToken1 = "cVXnVdTcM00lnKcKq4zrBn:APA91bFBMJQ87ryzNipCIVwlXOpwNl-3RWjOTw1Ei3yAFL6q3wr7bkVzRmXMeFYhtFYhEKlfogztMKTTRK4sVBLkLpCGh0NOCicbwqUjF2hJ-shta-lspkZBuTU5MWS6R2-cdBbmYvM1"
  // Samsung Galaxy S7
  val fcmToken2 = "fKloxEYmR7CinffPjQgUn2:APA91bE0-dyq0RRj9k1SFol8kO6xX39QnhQh2sHzZNTxRpZ_0cLlxhymlwA9TEKuIKOOLjG2bjPHjEv7iOA7fgWtB1sn1JX8oCev6j3EMHpmKfUZXybECpCUtUmqTSzWrpP-Yz6Awn2j"

  val firebase = new FirebaseHelper(firebaseConfPath)
  val usdToRubPath   = "https://iss.moex.com/iss/engines/currency/markets/selt/securities.jsonp?iss.meta=off&iss.only=marketdata&securities=CETS:USD000UTSTOM"
  val aviasalesPath6 = "https://lyssa.aviasales.ru/price_matrix?origin_iata=EVN&destination_iata=LED&depart_start=2023-01-06&depart_range=0"
  val aviasalesPath7 = "https://lyssa.aviasales.ru/price_matrix?origin_iata=EVN&destination_iata=LED&depart_start=2023-01-07&depart_range=0"

  val checkers = List(
    new Checker("USD", usdToRubPath, "marketdata.data[0][8]", GreaterComparer, desiredUsdRate, firebase, fcmToken2),
    new Checker("Aviasales-6", aviasalesPath6, "min(prices[].value)", LessComparer, desiredAviasalesRate, firebase, fcmToken2),
    new Checker("Aviasales-7", aviasalesPath7, "min(prices[].value)", LessComparer, desiredAviasalesRate, firebase, fcmToken2),
  )

  checkers.foreach(_.start())
}
