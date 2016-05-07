package tweet

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.Await
import scala.concurrent.duration._

import java.util.Calendar

object TweetAnalyzer extends App {

  implicit val system = ActorSystem("TweetAnalyzer")
  implicit val materializer = ActorMaterializer()

  val akka: Hashtag = Hashtag("#akka")

  val tweets: Source[Tweet, NotUsed] = Source {
    List[Tweet](
      Tweet(Author("Allen"), Calendar.getInstance().getTimeInMillis(), "Lagom is powered by #akka"),
      Tweet(Author("Christophe"), Calendar.getInstance().getTimeInMillis(), "Having fun with #akka stream"),
      Tweet(Author("Bob"), Calendar.getInstance().getTimeInMillis(), "Enjoying a sunny day")
    )
  }

  val authors: Source[Author, NotUsed] =
    tweets
      .filter(_.hashtags.contains(akka))
      .map(_.author)
  Await.result(authors.runWith(Sink.foreach(println)), 5 seconds)
  system.terminate()
}
