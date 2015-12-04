package com.example.actors

import akka.actor.{Actor, ActorLogging}
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import java.util.UUID

object TailCoordinator {
  object Messages {
    case class StartTail(filePath:String, reporterActor:ActorRef)
    case class StopTail(filePath:String)
  }
}

class TailCoordinator extends Actor with ActorLogging {
  
  
  def receive = {
  		case TailCoordinator.Messages.StartTail(file, reporter) => context.actorOf(TailActor.props(reporter, file), "TailActor-" + UUID.randomUUID().toString())
  }
  
  override def supervisorStrategy = OneForOneStrategy(10, 10 seconds){
    case e:ArithmeticException => resume
    case e:UnsupportedOperationException => stop
    case _ => restart
  }
  
}