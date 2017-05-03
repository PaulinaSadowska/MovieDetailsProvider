import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import data.{JsonParser, Movie}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object MoviesRestClient {

  private val API_KEY_PATH = "movieDb.apiKey"
  private val MOVIE_DETAILS_URL_FORMAT = "https://api.themoviedb.org/3/movie/%s?api_key=%s&append_to_response=credits"

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val wsClient = AhcWSClient()
    val apiKey = ConfigFactory.load().getString(API_KEY_PATH)
    val movieId = "680"
    val movieDetailsUrl = MOVIE_DETAILS_URL_FORMAT.format(movieId, apiKey)

    call(wsClient, movieDetailsUrl)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
      .onComplete({
        case Success(movie) => println(movie)
        case Failure(error) => println("An error has occured: " + error.getMessage)
      })
  }

  def call(wsClient: WSClient, movieDetailsUrl: String): Future[Movie] = {
    wsClient.url(movieDetailsUrl).get().map {
      response =>
        val body: String = response.body
        if (response.status != 200) {
          throw new Exception(s"Received unexpected status ${response.status} : ${response.body}")
        }
        JsonParser.toMovie(body)
    }
  }
}
