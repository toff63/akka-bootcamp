package com.example.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

object ValidationActor {
  def props(consoleWriter: ActorRef): Props = Props(new ValidationActor(consoleWriter))
}

class ValidationActor(consoleWriter: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case line: String =>
      if (noMessage(line)) nullInputError
      else if (isValid(line)) valid
      else validationError
      sender ! ConsoleMessages.ContinueProcessing
  }
  
  private def validationError = consoleWriter  ! ConsoleMessages.ValidationError("Invalid: input had odd number of characters.")
  private def nullInputError = consoleWriter ! ConsoleMessages.NullInputError("No input received.")
  private def valid: Unit = consoleWriter ! ConsoleMessages.InputSuccess("Thank you! Message was valid.")
  private def noMessage(line: String) = line.isEmpty()
  private def end(line: String) = line.toLowerCase() == ConsoleMessages.endCommand
  private def isValid(line: String): Boolean = line.length() % 2 == 0

}