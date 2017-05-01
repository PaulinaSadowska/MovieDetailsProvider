import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object MoviesRestClient {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val wsClient = AhcWSClient()
    val apiKey = ConfigFactory.load("application").getString("movieDb.apiKey")

    call(wsClient, apiKey)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
  }

  def call(wsClient: WSClient, apiKey: String): Future[Unit] = {
    val movieId = "150540"
    wsClient.url("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey + "&append_to_response=credits").get().map {
      response =>
        val body: String = response.body
        println(s"Got a response $body")
    }
  }
}
