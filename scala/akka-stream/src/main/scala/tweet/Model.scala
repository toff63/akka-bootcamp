package tweet

import akka.stream._
import akka.stream.scaladsl._
import akka.NotUsed

import java.util.Calendar

final case class Author(handle: String)
final case class Hashtag(name: String)
final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case word if word.startsWith("#") => Hashtag(word) }.toSet
}

object TweetSource {
  val tweets: Source[Tweet, NotUsed] = Source {
    List[Tweet](
      Tweet(Author("Allen"), Calendar.getInstance().getTimeInMillis(), "Lagom is powered by #akka"),
      Tweet(Author("Christophe"), Calendar.getInstance().getTimeInMillis(), "Having fun with #akka #stream"),
      Tweet(Author("Willis"), Calendar.getInstance().getTimeInMillis(), "Don't get me started! #bad_day"),
      Tweet(Author("Frank"), Calendar.getInstance().getTimeInMillis(), "Summer time!"),
      Tweet(Author("Bob"), Calendar.getInstance().getTimeInMillis(), "Enjoying a #sunny day")
    )
  }
}

