package com.example

import com.example.actors.CounterActor


object HelloSimpleMain {

  def main(args: Array[String]): Unit = {
    val initialActor = classOf[CounterActor].getName

    akka.Main.main(Array(initialActor))
  }

}
