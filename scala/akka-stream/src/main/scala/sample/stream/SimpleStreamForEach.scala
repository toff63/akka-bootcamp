import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.NotUsed

object SimpleStreamForEach extends App {
  implicit val system = ActorSystem("HelloWorld")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)
  source.runForeach(i => println(i))(materializer)
  Thread.sleep(500)
  system.terminate()
}
