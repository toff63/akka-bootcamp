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
  val fileName = new File(new File(".").getAbsolutePath() + "/Factorials.txt")
  val result: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toFile(fileName))
  Await.result(result, 5 seconds)
  print("Check your file here: " + fileName)
  system.terminate()
}

