package com.mitrakov.self.serverpush

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.StrictLogging
import io.burt.jmespath.jackson.JacksonRuntime
import sttp.client3.{HttpClientSyncBackend, Identity, SttpApi, SttpBackend}
import sttp.model.{MediaType, Method}
import java.time.{Duration, LocalDateTime}
import scala.util.Try

class Checker(name: String, method: Method, uri: String, jsonBody: Option[String], jmesPath: String, comparer: Comparer, desiredRate: Double, firebase: FirebaseHelper, recipientFcmToken: String)
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
      Thread.sleep(10000L)
    }
  }

  def makeRequest(): Either[String, String] = {
    val request = jsonBody match {
      case None       => basicRequest.method(method, uri"$uri")
      case Some(json) => basicRequest.method(method, uri"$uri").contentType(MediaType.ApplicationJson).body(json)
    }
    val response = request.send(sttp)
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
