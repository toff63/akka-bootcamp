package com.example.actors.camel

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import akka.Main.Terminator
import akka.actor.{ Actor, ActorLogging, ActorSystem, PoisonPill, Props, actorRef2Scala }
import akka.camel.{ CamelExtension, CamelMessage, Consumer, Producer }
import akka.Main.Terminator


object BasicAkkaCamel {
    def main(args: Array[String]): Unit = {
    val system = ActorSystem.create("main-system")
    val camelContext: DefaultCamelContext = CamelExtension(system).context
    camelContext.addRoutes(new RouteBuilder() {
      override def configure() {
        from("direct-vm://input").to("direct-vm://output")
      }
    })
    val app = system.actorOf(Props[MainActor])
    val terminator = system.actorOf(Props(classOf[Terminator], app), "app-terminator")
  }
}


class MainActor extends Actor with ActorLogging {
  val producer = context.actorOf(ProducingWorker.props("direct-vm://input"))
  val consumer = context.actorOf(ConsumingWorker.props("direct-vm://output"))

  producer ! "MSG"

  def receive = {
    case msg => 
      log info s"Round trip responded with $msg."
      self ! PoisonPill
      
  }
}

object ProducingWorker {
  def props(endpoint: String): Props = Props(classOf[ProducingWorker],endpoint)
}
class ProducingWorker(endpoint: String) extends Actor with Producer {
  override def endpointUri = endpoint
}

object ConsumingWorker {
  def props(endpoint: String): Props = Props(classOf[ConsumingWorker], endpoint)
}
class ConsumingWorker(endpoint: String) extends Actor with Consumer with ActorLogging {

  override def endpointUri = endpoint

  override def receive: Receive = {
    case CamelMessage(body, headers) =>
      log.info("Completed round for {} with {}.", endpoint, body)
      sender() ! CamelMessage("Consumed!", headers)
    case _ =>
  }
}