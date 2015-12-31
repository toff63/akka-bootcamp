package com.example.actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import com.example.actors.CounterActor.{Get , Tick}
import scala.concurrent.duration._

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class CounterSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A counter actor" must {
    "start out at zero" in {
      val c = system.actorOf(Props(new CounterActor))
      c ! Get
      expectMsg(0)
    }

    "increment using Tick" in {
      val c = system.actorOf(Props(new CounterActor))
      c ! Tick
      c ! Get
      expectMsg(1)
    }

    "not be dead slow" in {
      val c = system.actorOf(Props(new CounterActor))
      for (i <- 1 to 1000) c ! Tick
      within(1 second) {
        c ! Get
        expectMsg(1000)
      }
    }
  }
}