import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape


// A Suppliers table with 6 columns: id, name, street, city, state, zip
class MovieData(tag: Tag)
  extends Table[(Int, Boolean, Int, String, Double, Int,
    Int, Double, Int, Int, Int, Int, Int, String)](tag, "Movies") {

  // This is the primary key column:
  def id: Rep[Int] = column[Int]("movie_id", O.PrimaryKey)
  def adult: Rep[Boolean] = column[Boolean]("adult")
  def budget: Rep[Int] = column[Int]("budget")
  def originalLanguage: Rep[String] = column[String]("original_language")
  def popularity: Rep[Double] = column[Double]("popularity")
  def revenue: Rep[Int] = column[Int]("revenue")
  def runtime: Rep[Int] = column[Int]("runtime")
  def voteAverage: Rep[Double] = column[Double]("vote_average")
  def voteCount: Rep[Int] = column[Int]("vote_count")
  def AssAssAss: Rep[Int] = column[Int]("release_year")
  def directorId: Rep[Int] = column[Int]("director_id")
  def firstActorId: Rep[Int] = column[Int]("first_actor_id")
  def secondActorId: Rep[Int] = column[Int]("second_actor_id")
  def genres: Rep[String] = column[String]("genres")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, Boolean, Int, String, Double, Int,
    Int, Double, Int, Int, Int, Int, Int, String)] =
  (id, adult, budget, originalLanguage, popularity, revenue,
    runtime, voteAverage, voteCount, AssAssAss, directorId, firstActorId, secondActorId, genres)
}
