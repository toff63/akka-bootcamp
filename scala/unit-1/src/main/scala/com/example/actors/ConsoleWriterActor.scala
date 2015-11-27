package com.example.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

object ConsoleWriterActor {
  def props: Props = Props(new ConsoleWriterActor)
}

class ConsoleWriterActor extends Actor with ActorLogging{
  
	def receive = {
  	case ConsoleMessages.NullInputError(msg) => println(FontColor.ANSI_RED + msg + FontColor.ANSI_RESET)
  	case ConsoleMessages.ValidationError(msg) => println(FontColor.ANSI_RED + msg + FontColor.ANSI_RESET)
  	case ConsoleMessages.InputSuccess(line) => println(FontColor.ANSI_GREEN + line + FontColor.ANSI_RESET)
  	case msg => println(msg)
  }
}

object FontColor {
  val ANSI_RESET = "\u001B[0m"
  val ANSI_BLACK = "\u001B[30m"
  val ANSI_RED = "\u001B[31m"
  val ANSI_GREEN = "\u001B[32m"
  val ANSI_YELLOW = "\u001B[33m"
  val ANSI_BLUE = "\u001B[34m"
  val ANSI_PURPLE = "\u001B[35m"
  val ANSI_CYAN = "\u001B[36m"
  val ANSI_WHITE = "\u001B[37m"
}