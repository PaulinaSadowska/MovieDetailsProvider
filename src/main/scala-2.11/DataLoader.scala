import scala.io.Source

/**
  * Created by Paulina Sadowska on 03.05.2017.
  */
object DataLoader {

  private val DATA_PATH = "data/links.csv"
  val SEPARATOR: String = ","

  def loadMoviesIds(): Map[Int, Int] = {
    using(Source.fromFile(DATA_PATH)) {
      _.getLines
        .drop(1) //ignore first line (headers)
        .filter(_.split(SEPARATOR).length > 2) //ignore lines without theMovieDbId
        .map {
        line =>
          val fields = line.split(SEPARATOR)
          // format: (movieId, imdbId(ignored), theMovieDbId
          (fields(0).toInt, fields(2).toInt)
      }.toMap
    }
  }

  private def using[A <: {def close() : Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }
}
