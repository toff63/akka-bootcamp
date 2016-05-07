package sample.stream

import akka.actor.ActorSystem
import akka.{ NotUsed, Done }
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

object TimeBasedProcessing extends App {
  implicit val system = ActorSystem("TimeBasedProcessing")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  val done: Future[Done] =
    factorials
      .zipWith(Source(1 to 100))((num, idx) => s"$idx! => $num")
      .throttle(1, 1 second, 1, ThrottleMode.shaping)
      .runForeach(println)
  Await.result(done, 105 seconds)
  system.terminate()

}
