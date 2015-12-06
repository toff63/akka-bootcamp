package com.example

import com.example.scalafx.Main

import akka.actor.{ ActorLogging, ActorSystem }

object MonitoringMain {
  val system = ActorSystem("Main")

  def main(args: Array[String]): Unit = {
    new Main().main(Array())
  }
}

