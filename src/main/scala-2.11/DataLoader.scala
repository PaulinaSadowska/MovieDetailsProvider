import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by Paulina Sadowska on 03.05.2017.
  */
object DataLoader {

  def loadMoviesIds(): ArrayBuffer[Array[String]] = {
    val rows = ArrayBuffer[Array[String]]()
    using(Source.fromFile("/data/links.csv")) { source =>
      for (line <- source.getLines) {
        rows += line.split(",").map(_.trim)
      }
    }
    rows
  }

  private def using[A <: {def close() : Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }
}
