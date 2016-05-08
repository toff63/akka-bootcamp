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

object TweetAnalyzer extends App {

  implicit val system = ActorSystem("TweetAnalyzer")
  implicit val materializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  val authorStream: Future[Done] = AuthorSource.authors.runWith(Sink.foreach(println))
  val hashtagsStream: Future[Done] = HashtagSource.hashtags.runWith(Sink.foreach(println))
  val persistentStream: Future[Seq[IOResult]] = Future.sequence(PersistentSink.graph.run())

  Await.result(Future.sequence(Seq[Future[Object]](authorStream, hashtagsStream, persistentStream)), 5 seconds)
  system.terminate()

}

object TweetSource {
  val tweets: Source[Tweet, NotUsed] = Source {
    List[Tweet](
      Tweet(Author("Allen"), Calendar.getInstance().getTimeInMillis(), "Lagom is powered by #akka"),
      Tweet(Author("Christophe"), Calendar.getInstance().getTimeInMillis(), "Having fun with #akka #stream"),
      Tweet(Author("Bob"), Calendar.getInstance().getTimeInMillis(), "Enjoying a #sunny day")
    )
  }
}

object AuthorSource {
  val akka: Hashtag = Hashtag("#akka")
  val authors: Source[Author, NotUsed] =
    TweetSource.tweets
      .filter(_.hashtags.contains(akka))
      .map(_.author)
}

object HashtagSource {
  val hashtags: Source[Hashtag, NotUsed] = TweetSource.tweets.mapConcat(_.hashtags.toList)
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
