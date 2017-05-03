import java.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import data.{JsonParser, Movie}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

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
    val movieIds = DataLoader.loadMoviesIds()
    val movieId = movieIds.get(1)
    val movieDetailsUrl = MOVIE_DETAILS_URL_FORMAT.format(movieId.get, apiKey)

    val movies = fetchMovies(wsClient, movieIds, apiKey)

    wsClient.close()
    system.terminate()

    println("\nmovies fetched " + movies.length)
    println("movies ids on the list " + movieIds.size)
  }

  def fetchMovies(wsClient: WSClient, movieIds: Map[Int, Int], apiKey: String): ListBuffer[Movie] = {
    val movies = new ListBuffer[Movie]()
    for (ids <- movieIds) {
      val movieDetailsUrl = MOVIE_DETAILS_URL_FORMAT.format(ids._2, apiKey)
      retry(3)(
        movies += Await.result(call(wsClient, movieDetailsUrl), atMost = 10.second)
      )
      print(". ")
    }
    movies
  }

  def call(wsClient: WSClient, movieDetailsUrl: String): Future[Movie] = {
    wsClient.url(movieDetailsUrl).get().map {
      response =>
        val body: String = response.body
        if (response.status != 200) {
          if (response.status == 429) {
            val timeToSleep = response.allHeaders("Retry-After").last.toInt + 1
            println("wait for " + timeToSleep + " seconds")
            Thread.sleep(1000 * timeToSleep)
          }
          throw new Exception(s"Received unexpected status ${response.status} : ${response.body}")
        }
        else {
          JsonParser.toMovie(body)
        }
    }
  }

  // Returning T, throwing the exception on failure
  @annotation.tailrec
  def retry[T](n: Int)(fn: => T): T = {
    Try {
      fn
    } match {
      case Success(x) => x
      case _ if n > 1 =>
        println("retry")
        retry(n - 1)(fn)
      case Failure(e) => throw e
    }
  }


}
