package com.mitrakov.self.serverpush

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.StrictLogging
import io.burt.jmespath.jackson.JacksonRuntime
import sttp.client3.{HttpClientSyncBackend, Identity, SttpApi, SttpBackend}
import java.time.{Duration, LocalDateTime}
import scala.util.Try

class Checker(name: String, request: String, jmesPath: String, comparer: Comparer, desiredRate: Double, firebase: FirebaseHelper, recipientFcmToken: String)
  extends Thread with SttpApi with StrictLogging {

  val COOLDOWN_MINUTES = 180     // don't send duplicate messages during this time
  val sttp: SttpBackend[Identity, Any] = HttpClientSyncBackend()
  val jqRuntime = new JacksonRuntime()
  val mapper = new ObjectMapper()
  var lastSentMsgTime: LocalDateTime = LocalDateTime.MIN

  override def run(): Unit = {
    while (true) {
      val minutes = Duration.between(lastSentMsgTime, LocalDateTime.now).toMinutes
      if (minutes >= COOLDOWN_MINUTES) Try {
        makeRequest() match {
          case Right(json) =>
            val realRate = parseDouble(json, jmesPath)
            if (comparer.compare(realRate, desiredRate)) {
              firebase.sendMessage(recipientFcmToken, s"Tommy $name Checker", s"$realRate $comparer $desiredRate!")
              lastSentMsgTime = LocalDateTime.now()
            }
          case Left(error) => logger.error(error)
        }
      }
      Thread.sleep(5000L)
    }
  }

  def makeRequest(): Either[String, String] = {
    val response = basicRequest.get(uri"$request").send(sttp)
    val result = response.body
    logger.debug(result.toString)
    result
  }

  def parseDouble(json: String, jmesPath: String): Double = {
    val jq = jqRuntime.compile(jmesPath)
    val node = mapper.readTree(json)
    val result = jq.search(node)
    logger.info(s"$name result is $result")
    result.asDouble()
  }
}
