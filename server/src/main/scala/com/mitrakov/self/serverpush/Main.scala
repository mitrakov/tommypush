package com.mitrakov.self.serverpush

import sttp.model.Method

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
  val usdToRubPath  = "https://iss.moex.com/iss/engines/currency/markets/selt/securities.jsonp?iss.meta=off&iss.only=marketdata&securities=CETS:USD000UTSTOM"
  val aviasalesPath = "https://ariadne.aviasales.ru/api/gql"    // GET: "https://lyssa.aviasales.ru/price_matrix?origin_iata=EVN&destination_iata=LED&depart_start=2023-01-06&depart_range=0"; jq="min(prices[].value)"

  val checkers = List(
    new Checker("USD", Method.GET, usdToRubPath, None, "marketdata.data[0][8]", GreaterComparer, desiredUsdRate, firebase, fcmToken2),
    new Checker("Aviasales-Feb23", Method.POST, aviasalesPath, aviasalesJson(), "data.best_prices_v2.cheapest_direct.value", LessComparer, desiredAviasalesRate, firebase, fcmToken2)
  )

  checkers.foreach(_.start())

  def aviasalesJson(): Option[String] = Some {
    """{
       "variables" : {
          "bestPricesInput" : {
             "origin" : "LED",
             "destination" : "EVN",
             "one_way" : true,
             "dates" : {"depart_dates" : ["2023-02-23"]},
             "market" : "ru",
             "currency" : "rub"
          },
          "brand" : "AS",
          "fetchPriceChart" : false,
          "priceChartInput" : {
             "origin_city_iata" : "LED",
             "destination_city_iata" : "EVN",
             "one_way" : true,
             "dates" : {
                "depart_date_from" : "2023-02-23",
                "depart_date_to" : "2023-02-24",
                "return_date_from" : "2023-02-23",
                "return_date_to" : "2023-02-24"
             },
             "market" : "ru",
             "currency" : "rub",
             "filters" : {
                "direct" : true,
                "convenient" : false,
                "with_baggage" : false
             },
             "trip_class" : "Y"
          }
       },
       "query" : "\nquery GetMinPrices(\n  $bestPricesInput: BestPricesV2Input!\n  $priceChartInput: PriceChartV3Input!\n  $brand: Brand!\n  $fetchPriceChart: Boolean!\n  ) {\n  best_prices_v2(\n    input: $bestPricesInput\n    brand: $brand\n  ) {\n    cheapest {\n      ...priceFields        \n    }\n    cheapest_direct {\n      ...priceFields        \n    }\n    cheapest_convenient {\n      ...priceFields\n    }\n  }\n  price_chart_v3 (\n    input: $priceChartInput\n    brand: $brand\n  ) @include(if: $fetchPriceChart) {\n    prices {\n      price {\n        value\n        depart_date\n        return_date\n      }\n      stats {\n        depart_low\n        depart_value\n        return_low\n        return_value\n      }\n    }\n  }\n}\n\nfragment priceFields on Price {\n  depart_date\n  return_date\n  value\n  found_at\n  signature\n  convenient\n  segments {\n    transfers {\n      duration_seconds\n      visa_required\n      night_transfer\n      country_code\n      at\n      to \n    }\n  }\n}\n\n"
    }"""
  }
}
