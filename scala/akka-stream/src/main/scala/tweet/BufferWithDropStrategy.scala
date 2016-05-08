package tweet

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._

import java.util.Calendar
import java.io.File

/**
 * Back pressure will happen by default
 * with no change in code.
 * However you can create buffer if you want or need
 * and establish policy about how you want to handle overflows
 */
object BufferWithDropStrategy extends App {

  implicit val system = ActorSystem("TweetAnalyzer")
  implicit val materializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  val res = TweetSource.tweets
    .buffer(3, OverflowStrategy.dropHead)
    .map { tweet =>
      Thread.sleep(5000)
      tweet
    }
    .runWith(Sink.foreach(println))

  Await.result(res, 60 seconds)
  system.terminate()

}

