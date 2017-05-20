import slick.driver.H2Driver.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}


// A Suppliers table with 6 columns: id, name, street, city, state, zip
class MovieData(tag: Tag)
  extends Table[(Int, Boolean, String)](tag, "Movies") {

  // This is the primary key column:
  def id: Rep[Int] = column[Int]("movie_id", O.PrimaryKey)
  def adult: Rep[Boolean] = column[Boolean]("adult")
  def originalLanguage: Rep[String] = column[String]("original_language")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, Boolean, String)] =
  (id, adult, originalLanguage)
}
