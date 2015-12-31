package com.example.actors.future

import akka.actor.Actor
import akka.actor.Props
import com.example.actors.future.WebServiceClient.RetrieveRemoteData
import akka.actor.ActorLogging

object Main {

  def main(args: Array[String]): Unit = {
    val initialActor = classOf[Main].getName
    akka.Main.main(Array(initialActor))
  }
}

object MainMessage {
  case class Response(response:String)
}

class Main extends Actor with ActorLogging{
  import MainMessage._
  var numberOfExpectedResponse = 10

  override def preStart() = {
  log info "Seneding request to service"
  val client = context.actorOf(Props[WebServiceClient], "webservice-client") 
  for(i <- 1 to numberOfExpectedResponse) client ! RetrieveRemoteData("test")    
  }
  def receive = {
    case Response(response) => 
      println(s"Got response from client: $response")
      numberOfExpectedResponse = numberOfExpectedResponse - 1
      if(numberOfExpectedResponse == 0) context.stop(self)
  }
}