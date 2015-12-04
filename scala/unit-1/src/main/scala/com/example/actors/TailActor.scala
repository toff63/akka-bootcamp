package com.example.actors

import akka.actor.{ Actor, ActorLogging, ActorRef }
import scala.io.Source
import akka.actor.Props
import java.nio.file.Files
import java.nio.file.Path
import scala.collection.JavaConversions._
import java.nio.file.FileSystems
import java.nio.charset.Charset
import java.util.UUID

object TailActor {
  object Messages {
    case class FileWrite()
    case class FileError(fileName: String, reason: String)
    case class InitialRead()
  }

  def props(reporterActor: ActorRef, filePath: String): Props = Props(new TailActor(reporterActor, filePath))
}

class TailActor(reporterActor: ActorRef, filePath: String) extends Actor with ActorLogging {

  var numberOfReadLines: Int = 0
  context.actorOf(FileObserver.props(self, filePath), "FileObserver-" + UUID.randomUUID().toString()) ! "start"
  self ! TailActor.Messages.InitialRead

  def receive = {
    case TailActor.Messages.InitialRead => read
    case TailActor.Messages.FileWrite => read
    case TailActor.Messages.FileError(fileName, reason) =>
      reporterActor ! s"Tail error: $reason"
  }

  private def read = {
    val lines:Seq[String] = Files.readAllLines(FileSystems.getDefault().getPath(filePath), Charset.forName("UTF-8"))
    val text = lines.drop(numberOfReadLines).mkString("\n")
    numberOfReadLines = lines.size
    reporterActor ! text
  }
}