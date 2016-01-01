package com.example.actors.camel

import scala.annotation.tailrec
import scala.collection.JavaConversions.asScalaSet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import akka.actor.{ ActorLogging, PoisonPill, Props, actorRef2Scala }
import akka.camel.{ CamelMessage, Consumer }
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }

object ConsumingPublisher {
  def props(url: String) = Props(classOf[ConsumingPublisher], url)
}
class ConsumingPublisher(private val url: String)
    extends ActorPublisher[SearchItem] with Consumer with ActorLogging {
  import akka.stream.actor.ActorPublisherMessage._
  override def endpointUri: String = url

  var buffer = Vector.empty[SearchItem]

  override def receive: Receive = actorPublisherProtocol orElse {
    case CamelMessage(body: SearchResult, headers) =>
      log info s"received $body"
      buffer = buffer ++ body.items
      buffer = passOnNext(buffer, totalDemand)
      if(buffer.isEmpty){
        onComplete()
        self ! PoisonPill
      }
  }

  def actorPublisherProtocol: PartialFunction[Any, Unit] = {
    case Request(numberOfMessage) =>
      log info s"subscriber requested $numberOfMessage elements. Buffer currently contains ${buffer.size} elements"
      buffer = passOnNext(buffer, numberOfMessage)
      log info s"sent ${buffer.size} elements to subscribers"
    case Cancel => 
      log info s"Subscriber cancelled"
      self ! PoisonPill
  }

  def canCallOnNext(items:Vector[SearchItem]) = isActive &&  totalDemand > 0 && items.nonEmpty
  def remainMessageToDeliver(deliveredMessages:Int, demand:Long) = demand - deliveredMessages > 0
  

  @tailrec private def passOnNext( items:Vector[SearchItem], remainingDemand: Long): Vector[SearchItem] = {
    if (canCallOnNext(items)) {
      onNext(items.head)
      passOnNext(items.tail, remainingDemand - 1)
    } else {
      items
    }
  }
}