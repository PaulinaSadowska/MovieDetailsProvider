import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import network.ApiHelper
import play.api.libs.ws.ahc.AhcWSClient

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object MoviesRestClient {

  private val API_KEY_PATH = "movieDb.apiKey"

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val wsClient = AhcWSClient()
    val apiKey = ConfigFactory.load().getString(API_KEY_PATH)
    val movieIds = FileLoader.loadMoviesIds()

    val movies = ApiHelper.fetchMovies(wsClient, movieIds, apiKey)

    wsClient.close()
    system.terminate()

    println("\nmovies fetched " + movies.length)
    println("movies ids on the list " + movieIds.size)
  }

}
