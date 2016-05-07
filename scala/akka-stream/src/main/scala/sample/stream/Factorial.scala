package sample.stream

import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.NotUsed
import akka.util.ByteString
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import java.io.File

object Factorial extends App {
  implicit val system = ActorSystem("HelloWorld")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  val fileName = new File(".").getAbsolutePath() + "/Factorials.txt"
  val result: Future[IOResult] = factorials.map(_.toString).runWith(lineSink(fileName))
  Await.result(result, 5 seconds)
  print("Check your file here: " + fileName)
  system.terminate()

  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toFile(new File(filename)))(Keep.right)
}

