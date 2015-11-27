package com.example

import com.example.actors.ConsoleReaderActor
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import com.example.actors.ConsoleWriterActor
import akka.dispatch.sysmsg.Terminate
import akka.actor.Terminated

/**
 * This is actually just a small wrapper around the generic launcher
 * class akka.Main, which expects only one argument: the class name of
 * the application?s main actor. This main method will then create the
 * infrastructure needed for running the actors, start the given main
 * actor and arrange for the whole application to shut down once the main
 * actor terminates.
 *
 * Thus you could also run the application with a
 * command similar to the following:
 * java -classpath  akka.Main com.example.actors.HelloWorldActor
 *
 * @author alias
 */
object HelloSimpleMain {

  def main(args: Array[String]): Unit = {
    val consoleStartingActor = classOf[ConsoleStartingActor].getName
    akka.Main.main(Array(consoleStartingActor))
  }
}

class ConsoleStartingActor extends Actor with ActorLogging {
	override def preStart(): Unit = {
			val writer = context.actorOf(ConsoleWriterActor.props, "writer")
			val reader = context.actorOf(ConsoleReaderActor.props(writer))
			reader ! ConsoleReaderActor.startCommand
			context watch reader

	}
	def receive = {
	  case Terminated(_) => context.stop(self)
	}
}
