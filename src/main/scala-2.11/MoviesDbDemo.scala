import scala.concurrent.{Await, Future}

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Paulina Sadowska on 20.05.2017.
  */
object MoviesDbDemo {

  def main(args: Array[String]): Unit = {

    val db = Database.forConfig("sqlite")
    try {

      // The query interface for the Movies table
      val moviesTable = TableQuery[MovieData]

      val setupAction: DBIO[Unit] = DBIO.seq(
        // Create the schema by combining the DDLs for the Movie
        // table using the query interfaces
       // moviesTable.schema.create,

        // Insert some suppliers
        moviesTable += (101, false, "en")
      )
      val setupFuture: Future[Unit] = db.run(setupAction)

      val f = setupFuture.flatMap { _ =>
        // Insert some coffees (using JDBC's batch insert feature)
        val insertAction: DBIO[Option[Int]] = moviesTable ++= Seq(
          (103, false, "gb"),
          (105, true, "pl")
        )

        val insertAndPrintAction: DBIO[Unit] = insertAction.map { insertResult =>
          // Print the number of rows inserted
          insertResult foreach { numRows =>
            println(s"Inserted $numRows rows into the Movies table")
          }
        }

        val allMoviesAction: DBIO[Seq[(Int, Boolean, String)]] =
          moviesTable.result

        val combinedAction: DBIO[Seq[(Int, Boolean, String)]] =
          insertAndPrintAction >> allMoviesAction

        val combinedFuture: Future[Seq[(Int, Boolean, String)]] =
          db.run(combinedAction)

        combinedFuture.map { allMovies =>
          allMovies.foreach(println)
        }.flatMap { _ =>

          /* Filtering / Where */

          // Construct a query where the price of Coffees is > 9.0
          val filterQuery: Query[MovieData, (Int, Boolean, String), Seq] =
            moviesTable.filter(_.id > 101)

          // Print the SQL for the filter query
          println("Generated SQL for filter query:\n" + filterQuery.result.statements)

          // Execute the query and print the Seq of results
          db.run(filterQuery.result.map(println))

        }
      }
      Await.result(f, Duration.Inf)
    } finally db.close
  }

}
