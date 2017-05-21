import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import data.db.MovieData
import network.ApiHelper
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._


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
    val allMovieIds = FileLoader.loadMoviesIds()

    val movieIds = allMovieIds.take(1)
    val movies = ApiHelper.fetchMovies(wsClient, movieIds, apiKey)
    val db = Database.forConfig("sqlite")
    val m = movies.head
    try {
      // The query interface for the Movies table
      val moviesTable = TableQuery[MovieData]

      val addMoviesAction: DBIO[Unit] = DBIO.seq(
        moviesTable += (m.id, m.adult, m.budget, m.original_language,
          m.popularity, m.revenue, m.runtime, m.vote_average,
          m.vote_count, m.releaseYear, m.directorId, m.firstActorId,
          m.secondActorId, m.genreIds)
      )
      val addMovieFuture: Future[Unit] = db.run(addMoviesAction)
      val f = addMovieFuture
      Await.result(f, Duration.Inf)
    } finally db.close

    wsClient.close()
    system.terminate()

    println("\nmovies fetched " + movies.length)
    println("movies ids on the list " + movieIds.size)

  }

}
