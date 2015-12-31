package com.example.actors.future

import akka.actor.{ Actor, ActorLogging }
import com.example.actors.future.FutureHandling._
import scala.concurrent.Future
import scala.util.Random
import com.example.actors.future.FutureHandling._
import scala.concurrent.duration.Duration._
import scala.concurrent.duration._
import com.example.actors.future.WebServiceClient._
import akka.actor.Props
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.event.LoggingReceive
import java.util.UUID

object WebServiceClient {
  case class RetrieveRemoteData(req: String)
  case class ServiceResponse(originalSender: ActorRef, response: String)
  case class ServiceFailure(originalSender: ActorRef, request: String, error: Throwable)
}

class WebServiceClient extends Actor with ActorLogging with NoRetry {
  import MainMessage._
  var counter = 0
  def receive = {
    case RetrieveRemoteData(req) =>
      counter += 1
      context.actorOf(Props[FutureHandling], s"future-handler-$counter") ! Request(sender, req)
    case ServiceResponse(originalSender, response) => originalSender ! Response(response)
    case ServiceFailure(originalSender, request, error) =>
      if (shouldRetry(RetryContext(request, error, 0))) {
        log.error(error, "failed to call service")
        self.tell(RetrieveRemoteData(request), originalSender)
      } else originalSender ! Response("Service Unavailable.")
  }
}

object FutureHandling {
  object ClientTimeout
  case class Request(originalSender: ActorRef, req: String)
}

class FutureHandling extends Actor with ActorLogging {
  import WebServiceClient._

  implicit val ec: ExecutionContext = context.system.dispatchers.lookup("webservice-dispatcher")

  val timeoutMessager = context.system.scheduler.scheduleOnce(250 milliseconds)(self ! FutureHandling.ClientTimeout)

  def receive = {
    case Request(originalSender, request) =>
      log debug "Calling service"
      val currentSender = sender
      callRemoteServer(request).map { result => sendResponseAndshutdown(currentSender, ServiceResponse(originalSender, result))}
                               .recover (logAndInformSender(currentSender, request))

    case ClientTimeout =>
      log info "Timeout. Stopping myself"
      context.stop(self)
  }

  def logAndInformSender(originalSender:ActorRef, request:String):PartialFunction[Throwable, Unit] = {
    case t: Throwable =>
            log.error(t, "failed to call service")
            sendResponseAndshutdown(originalSender, ServiceFailure(originalSender, request, t))
  }
  
  def sendResponseAndshutdown(originalSender: ActorRef, msg: Any) = {
    originalSender ! msg
    if (context != null) context.stop(self)
  }

  def callRemoteServer(request: String): Future[String] = Future {
    val r: Random = new Random()
    Thread.sleep(r.nextInt(550))
    "Result"
  }
}