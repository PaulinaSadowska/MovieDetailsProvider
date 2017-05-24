import data.Movie
import data.db.MovieData
import slick.dbio.DBIOAction
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

/**
  * Created by Paulina Sadowska on 24.05.2017.
  */
class DatabaseActions(moviesTable: TableQuery[MovieData]) {

  private def toMovieData(m: Movie) = {
    (m.id, m.adult, m.budget, m.original_language,
      m.popularity, m.revenue, m.runtime, m.vote_average,
      m.vote_count, m.releaseYear, m.directorId, m.firstActorId,
      m.secondActorId, m.genreIds)
  }

  def deleteMovies(): DBIOAction[Unit, NoStream, Nothing] = {
    val deleteMoviesAction: DBIO[Unit] = DBIO.seq(
      moviesTable.delete
    )
    deleteMoviesAction
  }

  def addMovies(movies: List[Movie]): DBIOAction[Option[Int], NoStream, Nothing] = {
    var moviesSeq: Seq[(Int, Boolean, Int, String, Double, Int,
      Int, Double, Int, Int, Int, Int, Int, String)] = Seq.empty
    for (elem <- movies) {
      moviesSeq :+= toMovieData(elem)
    }
    val addMoviesAction: DBIO[Option[Int]] = moviesTable ++= moviesSeq
    addMoviesAction
  }
}
