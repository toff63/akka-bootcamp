package tweet

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.stream._
import akka.util.ByteString
import akka.stream.scaladsl._

import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._

import java.util.Calendar
import java.io.File

object GraphFlow extends App {

  implicit val system = ActorSystem("TweetAnalyzer")
  implicit val materializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  Await.result(Future.sequence(PersistentSink.graph.run()), 5 seconds)
  system.terminate()

}

object PersistentSink {
  val writeAuthors: Sink[Author, Future[IOResult]] = writeToFile(fullName("author"), Flow[Author].map(_.handle))
  val writeHashtags: Sink[Hashtag, Future[IOResult]] = writeToFile(fullName("hashtags"), Flow[Hashtag].map(_.name))

  val graph: RunnableGraph[Seq[Future[IOResult]]] =
    RunnableGraph.fromGraph(GraphDSL.create(writeAuthors, writeHashtags)(Seq(_, _)) { implicit builder =>
      (writeAuthorsSink, writeHashtagsSink) =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[Tweet](2))
        TweetSource.tweets ~> broadcast ~> Flow[Tweet].map(_.author) ~> writeAuthorsSink.in
        broadcast ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtagsSink.in
        ClosedShape
    })

  def writeToFile[T](fileName: String, flow: Flow[T, String, NotUsed]): Sink[T, Future[IOResult]] =
    flow
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toFile(new File(fileName)))(Keep.right)

  def fullName(fileName: String): String = new File(".").getAbsolutePath() + s"/${fileName}.txt"
}

