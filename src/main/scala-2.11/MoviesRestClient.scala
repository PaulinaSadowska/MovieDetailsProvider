import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import data.db.MovieData
import network.MovieDbApi
import play.api.libs.ws.ahc.AhcWSClient
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object MoviesRestClient {

  private val API_KEY_PATH = "movieDb.apiKey"
  private val DATA_SLICE_SIZE = 39 //API limit - 40 requests per 10 seconds

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val wsClient = AhcWSClient()
    val apiKey = ConfigFactory.load().getString(API_KEY_PATH)
    val allMovieIds = FileLoader.loadMoviesIds()

    val db = Database.forConfig("sqlite")

    try {
      //clean database
      val moviesTable = TableQuery[MovieData]
      val dbActions = new DatabaseActions(moviesTable)
      val deleteMoviesFuture: Future[Unit] = db.run(dbActions.deleteMovies())
      Await.result(deleteMoviesFuture, Duration.Inf)

      //fetch data
      for (i <- DATA_SLICE_SIZE until allMovieIds.size + DATA_SLICE_SIZE by DATA_SLICE_SIZE) {
        val movieIds = allMovieIds.slice(i - DATA_SLICE_SIZE, i)
        val movies = new MovieDbApi(wsClient).fetchMovies(movieIds, apiKey)
        println("\nmovies fetched " + movies.length)
        val addMoviesFuture: Future[Option[Int]] = db.run(dbActions.addMovies(movies))
        Await.result(addMoviesFuture, Duration.Inf)
      }
    }
    finally db.close

    wsClient.close()
    system.terminate()
  }
}
