package com.example.actors.camel

import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.dataformat.JsonLibrary
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.annotation.{ JsonIgnoreProperties, JsonProperty }

import akka.actor.{ ActorRef, ActorSystem }
import akka.camel.CamelExtension
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{ Sink, Source }

@JsonIgnoreProperties(ignoreUnknown = true)
case class SearchResult(@JsonProperty("items") items: java.util.HashSet[SearchItem])

@JsonIgnoreProperties(ignoreUnknown = true)
case class SearchItem(@JsonProperty("link") link: String)

trait GoogleConfig {
  val apiKey = "AIzaSyAIJYWk-Q0DP8rvEQVJ05N2SVexPYDTjAo"
}

class SearchRoute(system: ActorSystem, materializer: ActorMaterializer) extends GoogleConfig {
  implicit val logger = LoggerFactory.getLogger(classOf[SearchRoute])

  def registerRoute():ActorRef = {
    CamelExtension(system).context.addRoutes(new RouteBuilder() {
      override def configure() {
        from("direct-vm://googler")
          .enrich("direct-vm://google-call")
          .choice()
          .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
          .unmarshal().json(JsonLibrary.Jackson, classOf[SearchResult])
          .to("direct-vm://googled")
          .otherwise()
          .setBody(constant("Fail - boom boom!"))
          .to("direct-vm://googled")

        from("direct-vm://google-call")
          .setHeader(
            Exchange.HTTP_QUERY,
            simple("key=" + apiKey + "&cx=001733240814555448082:yqsjy6oesoq&q=${body}"))
          .to("https4://www.googleapis.com/customsearch/v1?throwExceptionOnFailure=false")
      }
    })
    val consumingPublisher = system.actorOf(ConsumingPublisher.props("direct-vm://googled"), "consumingPublisher")
    Source.fromPublisher(ActorPublisher[SearchItem](consumingPublisher))
      .to(Sink.foreach(msg => println("Sank item {}.", msg.toString)))
      .run()(materializer)
      
    system.actorOf(ProducingWorker.props("direct-vm://googler"), "producer")
  }
}

