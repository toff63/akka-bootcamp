package tweet

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.stream._
import akka.util.ByteString
import akka.stream.scaladsl._

import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._

import java.io.File

object SimpleFlow extends App {

  implicit val system = ActorSystem("TweetAnalyzer")
  implicit val materializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  val authorStream: Future[Done] = AuthorSource.authors.runWith(Sink.foreach(println))
  val hashtagsStream: Future[Done] = HashtagSource.hashtags.runWith(Sink.foreach(println))

  Await.result(Future.sequence(Seq(authorStream, hashtagsStream)), 5 seconds)
  system.terminate()

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

