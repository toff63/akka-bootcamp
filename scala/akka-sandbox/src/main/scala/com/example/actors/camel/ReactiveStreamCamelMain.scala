package com.example.actors.camel

import akka.Main.Terminator
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, actorRef2Scala }
import akka.stream.ActorMaterializer

object ReactiveStreamCamelMain {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem.create("main-system")
    implicit val materializer = ActorMaterializer.create(system)
    val app = system.actorOf(ReactiveStreamCamelActor.props(new SearchRoute(system, materializer)), "app")
    val terminator = system.actorOf(Props(classOf[Terminator], app), "app-terminator")
  }

}

object ReactiveStreamCamelActor {
  def props(route:SearchRoute) = Props(classOf[ReactiveStreamCamelActor], route)
}

class ReactiveStreamCamelActor(route:SearchRoute) extends Actor with ActorLogging {
  val producer:ActorRef = route.registerRoute()

  producer ! "Christophe Marchal"

  def receive = {
    case msg => 
      log info s"Round trip responded with $msg."
      self ! PoisonPill
      
  }
}

