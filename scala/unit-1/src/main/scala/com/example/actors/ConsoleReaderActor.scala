package com.example.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Scanner
import akka.actor.ActorRef
import akka.actor.Props

object ConsoleMessages {
  case class Line(line: String)
  case class ContinueProcessing()
  case class InputSuccess(line: String)
  case class NullInputError(line: String)
  case class ValidationError(line: String)
}

object ConsoleReaderActor {
  def props(consoleWriter: ActorRef): Props = Props(new ConsoleReaderActor(consoleWriter))
  val startCommand: String = "start"
}

class ConsoleReaderActor(consoleWriter: ActorRef) extends Actor with ActorLogging {

  val endCommand: String = "exit"
  def receive = {
    case ConsoleReaderActor.startCommand =>
      println("Write whatever you want into the console!")
      println("Some entries will pass validation, and some won't...\n\n")
      println("Type 'exit' to quit this application at any time.\n")
      loop
    case ConsoleMessages.NullInputError(line) =>
      consoleWriter ! ConsoleMessages.NullInputError(line)
      loop
    case ConsoleMessages.ValidationError(line) =>
      consoleWriter ! ConsoleMessages.ValidationError(line)
      loop
    case msg => loop

  }

  private def loop() {
    val scanner: Scanner = new Scanner(System.in);
    processLine(scanner.nextLine())
  }
  private def processLine(line: String): Unit = {
    if (noMessage(line)) nullInputError
    else if (end(line)) stop
    else if (isValid(line)) printAndContinue
    else validationError
  }
  
  private def validationError = self ! ConsoleMessages.ValidationError("Invalid: input had odd number of characters.")
  private def nullInputError = self ! ConsoleMessages.NullInputError("No input received.")
  private def stop = context.stop(self)
  private def printAndContinue: Unit = {
    consoleWriter ! ConsoleMessages.InputSuccess("Thank you! Message was valid.")
    self ! ConsoleMessages.ContinueProcessing
  }
  private def noMessage(line: String) = line.isEmpty()
  private def end(line: String) = line.toLowerCase() == endCommand
  private def isValid(line: String): Boolean = line.length() % 2 == 0

}