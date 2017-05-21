import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import data.Movie
import data.db.MovieData
import network.ApiHelper
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.{Await, Awaitable, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.backend.DatabasePublisher
import slick.dbio.DBIOAction
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.collection.mutable.ListBuffer


/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object MoviesRestClient {

  private val API_KEY_PATH = "movieDb.apiKey"

  private val DATA_SLICE_SIZE = 39 //API limit - 40 requests per 10 seconds

  def toMovieData(m: Movie) = {
    (m.id, m.adult, m.budget, m.original_language,
      m.popularity, m.revenue, m.runtime, m.vote_average,
      m.vote_count, m.releaseYear, m.directorId, m.firstActorId,
      m.secondActorId, m.genreIds)
  }

  def deleteMoviesAction(moviesTable: TableQuery[MovieData]): DBIOAction[Unit, NoStream, Nothing] = {
    val deleteMoviesAction: DBIO[Unit] = DBIO.seq(
      moviesTable.delete
    )
    deleteMoviesAction
  }

  def addMoviesAction(movies: List[Movie], moviesTable: TableQuery[MovieData]): DBIOAction[Option[Int], NoStream, Nothing] = {
    var moviesSeq: Seq[(Int, Boolean, Int, String, Double, Int,
      Int, Double, Int, Int, Int, Int, Int, String)] = Seq.empty
    for (elem <- movies) {
      moviesSeq :+= toMovieData(elem)
    }
    val addMoviesAction: DBIO[Option[Int]] = moviesTable ++= moviesSeq
    addMoviesAction
  }

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
      val deleteMoviesFuture: Future[Unit] = db.run(deleteMoviesAction(moviesTable))
      Await.result(deleteMoviesFuture, Duration.Inf)

      //fetch data
      for (i <- DATA_SLICE_SIZE until allMovieIds.size + DATA_SLICE_SIZE by DATA_SLICE_SIZE) {
        val movieIds = allMovieIds.slice(i - DATA_SLICE_SIZE, i)
        val movies = ApiHelper.fetchMovies(wsClient, movieIds, apiKey)
        println("\nmovies fetched " + movies.length)
        val addMoviesFuture: Future[Option[Int]] = db.run(addMoviesAction(movies, moviesTable))
        Await.result(addMoviesFuture, Duration.Inf)
      }
    }
    finally db.close

    wsClient.close()
    system.terminate()

  }

}
