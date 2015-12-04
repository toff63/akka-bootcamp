package com.example.actors

import akka.actor.ActorRef
import java.nio.file.Path
import java.nio.file.FileSystems
import java.nio.file.WatchEvent
import java.nio.file.StandardWatchEventKinds
import java.io.Closeable
import scala.collection.JavaConversions._
import scala.util.Try
import java.nio.file.WatchService
import akka.actor.Actor
import akka.actor.Props

object FileObserver {
  def props(tailActor: ActorRef, absoluteFilePath: String):Props = Props(new FileObserver(tailActor, absoluteFilePath))
}

class FileObserver(tailActor: ActorRef, absoluteFilePath: String) extends Actor {
  val watcher = FileSystems.getDefault.newWatchService()

  override def preStart(): Unit =  try {
    val path: Path = FileSystems.getDefault().getPath(absoluteFilePath)
    println("Monitoring " + path.getParent)
    path.getParent.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
  } catch {
    case t: Throwable =>
      t.printStackTrace()
      tailActor ! TailActor.Messages.FileError(absoluteFilePath, t.getMessage)
  }

  def receive = {
    case _ =>
      watcher.take()
      self ! "continue"
      tailActor ! TailActor.Messages.FileWrite
  }
}