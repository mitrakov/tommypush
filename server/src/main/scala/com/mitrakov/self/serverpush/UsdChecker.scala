package com.mitrakov.self.serverpush

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.StrictLogging
import io.burt.jmespath.jackson.JacksonRuntime
import sttp.client3.{HttpClientSyncBackend, SttpApi}
import java.time.{Duration, LocalDateTime}
import scala.util.Try

class UsdChecker(desiredRate: Double, helper: FirebaseHelper, recipientFcmToken: String) extends Runnable with SttpApi with StrictLogging {
  val COOLDOWN_MINUTES = 180     // don't send duplicate messages during this time
  val sttp = HttpClientSyncBackend()
  val jqRuntime = new JacksonRuntime()
  val mapper = new ObjectMapper()
  var lastSentMsgTime: LocalDateTime = LocalDateTime.parse("2000-01-01T00:00:00")

  override def run(): Unit = {
    while (true) {
      val minutes = Duration.between(lastSentMsgTime, LocalDateTime.now).toMinutes
      if (minutes >= COOLDOWN_MINUTES) Try {
        makeRequest() match {
          case Right(json) =>
            val realRate = parseDouble(json, jmesPath = "marketdata.data[0][8]")
            if (realRate >= desiredRate) {
              helper.sendMessage(recipientFcmToken, "Tommy Push Notification", s"$realRate ≥ $desiredRate!")
              lastSentMsgTime = LocalDateTime.now()
            }
          case Left(error) => logger.error(error)
        }
      }
      Thread.sleep(5000L)
    }
  }

  def makeRequest(): Either[String, String] = {
    val response = basicRequest
      .get(uri"https://iss.moex.com/iss/engines/currency/markets/selt/securities.jsonp?iss.meta=off&iss.only=marketdata&securities=CETS:USD000UTSTOM")
      .send(sttp)
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
}
