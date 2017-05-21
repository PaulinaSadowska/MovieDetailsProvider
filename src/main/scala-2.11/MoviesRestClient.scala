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
    try {
      // The query interface for the Movies table
      val moviesTable = TableQuery[MovieData]

      val deleteMoviesAction: DBIO[Unit] = DBIO.seq(
        moviesTable.delete
      )

      val movieIds = allMovieIds.take(2)
      val movies = ApiHelper.fetchMovies(wsClient, movieIds, apiKey)

      var moviesSeq: Seq[(Int, Boolean, Int, String, Double, Int,
        Int, Double, Int, Int, Int, Int, Int, String)] = Seq.empty
      for (elem <- movies) {
        moviesSeq :+= toMovieData(elem)
      }
      val addMoviesAction: DBIO[Option[Int]] = moviesTable ++= moviesSeq
      val deleteMoviesFuture: Future[Unit] = db.run(deleteMoviesAction)
      val f = deleteMoviesFuture.flatMap { _ =>
        db.run(addMoviesAction)
      }
      Await.result(f, Duration.Inf)
      println("\nmovies fetched " + movies.length)
      println("movies ids on the list " + movieIds.size)
    } finally db.close

    wsClient.close()
    system.terminate()

  }

}
