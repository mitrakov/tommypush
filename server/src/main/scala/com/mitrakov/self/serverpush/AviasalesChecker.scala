package com.mitrakov.self.serverpush

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.StrictLogging
import io.burt.jmespath.jackson.JacksonRuntime
import sttp.client3.{HttpClientSyncBackend, SttpApi}

import java.time.{Duration, LocalDate, LocalDateTime}
import scala.util.Try

class AviasalesChecker(date: LocalDate, desiredRate: Int, helper: FirebaseHelper, recipientFcmToken: String) extends Runnable with SttpApi with StrictLogging {
  val COOLDOWN_MINUTES = 180     // don't send duplicate messages during this time
  val sttp = HttpClientSyncBackend()
  val jqRuntime = new JacksonRuntime()
  val mapper = new ObjectMapper()
  var lastSentMsgTime: LocalDateTime = LocalDateTime.MIN

  override def run(): Unit = {
    while (true) {
      val minutes = Duration.between(lastSentMsgTime, LocalDateTime.now).toMinutes
      if (minutes >= COOLDOWN_MINUTES) Try {
        makeRequest() match {
          case Right(json) =>
            val realRate = parseDouble(json, jmesPath = "min(prices[].value)")
            if (realRate <= desiredRate) {
              helper.sendMessage(recipientFcmToken, "Tommy: Aviasales Checker", s"$realRate ≤ $desiredRate!")
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
      .get(uri"https://lyssa.aviasales.ru/price_matrix?origin_iata=LED&destination_iata=EVN&depart_start=$date&depart_range=0")
      .send(sttp)
    val result = response.body
    logger.debug(result.toString)
    result
  }

  def parseDouble(json: String, jmesPath: String): Double = {
    val jq = jqRuntime.compile(jmesPath)
    val node = mapper.readTree(json)
    val result = jq.search(node)
    logger.info(s"Aviasales result is $result")
    result.asDouble()
  }
}
