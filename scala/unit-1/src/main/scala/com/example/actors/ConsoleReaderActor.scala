package com.example.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Scanner
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.ActorPath

object ConsoleMessages {
  case class Line(line: String)
  case class ContinueProcessing()
  case class InputSuccess(line: String)
  case class NullInputError(line: String)
  case class ValidationError(line: String)
  val startCommand: String = "start"
  val endCommand: String = "exit"
}

object ConsoleReaderActor {
  def props(): Props = Props(new ConsoleReaderActor())
}

class ConsoleReaderActor() extends Actor with ActorLogging {

  def receive = {
    case ConsoleMessages.startCommand =>
      println("Enter the URI of a file on your disk")
      println("like " + new java.io.File(".").getAbsolutePath)
      loop
    case msg => loop

  }

  private def loop() {
    val scanner: Scanner = new Scanner(System.in);
    processLine(scanner.nextLine())
  }
  private def processLine(line: String): Unit = {
    if (end(line)) stop
    else {
      context.actorSelection("akka://Main/user/app/validator") ! line
    }
  }
  
  private def stop = context.stop(self)
  private def end(line: String) = !line.isEmpty() && line.toLowerCase() == ConsoleMessages.endCommand

}
