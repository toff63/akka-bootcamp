package com.example.actors

import akka.actor.{Actor, ActorLogging}
import com.example.actors.CounterActor.Tick
import com.example.actors.CounterActor.Get

object CounterActor{
  case object Tick
  case object Get
}
class CounterActor extends Actor with ActorLogging {
  var counter:Int = 0
  
  def receive = {
  		case Tick => 
  		  counter += 1
  		case Get => sender ! counter 
  }
}
