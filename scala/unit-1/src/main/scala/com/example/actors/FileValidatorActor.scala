package com.example.actors

import akka.actor.{ Actor, ActorLogging }
import akka.actor.ActorRef
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileSystems
import akka.actor.Props

object FileValidator {
  def props(consoleWriter: ActorRef):Props = Props(new FileValidatorActor(consoleWriter))
}


class FileValidatorActor(consoleWriter: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case line: String =>
      if (noMessage(line)) nullInputError
      else if (isValid(line)) valid(line)
      else validationError(line)
      sender ! ConsoleMessages.ContinueProcessing
  }

  private def validationError(file:String) = consoleWriter ! ConsoleMessages.ValidationError(s"$file isn't a valid file URI")
  private def nullInputError = consoleWriter ! ConsoleMessages.NullInputError("No input received.")
  private def valid(file:String): Unit = {
    consoleWriter ! ConsoleMessages.InputSuccess(s"Starting to process $file")
    context.actorSelection("akka://Main/user/app/tailCoordinator") ! TailCoordinator.Messages.StartTail(file, consoleWriter)
  }
  private def noMessage(line: String) = line.isEmpty()
  private def end(line: String) = line.toLowerCase() == ConsoleMessages.endCommand
  private def isValid(filePath: String): Boolean = Files.exists(FileSystems.getDefault().getPath(filePath))
}
