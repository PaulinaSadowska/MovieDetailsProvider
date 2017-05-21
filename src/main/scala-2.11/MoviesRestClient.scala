import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import data.Movie
import data.db.MovieData
import network.ApiHelper
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._

import scala.collection.mutable.ListBuffer


/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object MoviesRestClient {

  private val API_KEY_PATH = "movieDb.apiKey"

  private val DATA_SLICE_SIZE = 10

  def toMovieData(m: Movie) = {
    (m.id, m.adult, m.budget, m.original_language,
      m.popularity, m.revenue, m.runtime, m.vote_average,
      m.vote_count, m.releaseYear, m.directorId, m.firstActorId,
      m.secondActorId, m.genreIds)
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val wsClient = AhcWSClient()
    val apiKey = ConfigFactory.load().getString(API_KEY_PATH)
    val allMovieIds = FileLoader.loadMoviesIds()

    val db = Database.forConfig("sqlite")

    //clean database
    try {
      // The query interface for the Movies table
      val moviesTable = TableQuery[MovieData]

      val deleteMoviesAction: DBIO[Unit] = DBIO.seq(
        moviesTable.delete
      )
      val deleteMoviesFuture: Future[Unit] = db.run(deleteMoviesAction)
      val f = deleteMoviesFuture
      Await.result(f, Duration.Inf)

      for (i <- DATA_SLICE_SIZE to allMovieIds.size + DATA_SLICE_SIZE - 1 by DATA_SLICE_SIZE) {
        val movieIds = allMovieIds.slice(i - DATA_SLICE_SIZE, i)
        val movies = ApiHelper.fetchMovies(wsClient, movieIds, apiKey)
        println("\nmovies fetched " + movies.length)

        var moviesSeq: Seq[(Int, Boolean, Int, String, Double, Int,
          Int, Double, Int, Int, Int, Int, Int, String)] = Seq.empty
        for (elem <- movies) {
          moviesSeq :+= toMovieData(elem)
        }
        val addMoviesAction: DBIO[Option[Int]] = moviesTable ++= moviesSeq
        val addMoviesFuture: Future[Option[Int]] = db.run(addMoviesAction)
        val f = addMoviesFuture
        Await.result(f, Duration.Inf)
      }
    }
    finally db.close

    wsClient.close()
    system.terminate()

  }

}
