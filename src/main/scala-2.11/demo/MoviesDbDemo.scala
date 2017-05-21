package demo

import data.db.MovieData
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

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
        moviesTable += (108, false, 1, "en", 2.33, 3, 96, 7.88, 234, 1992, 22, 12,122, "action")
      )
      val setupFuture: Future[Unit] = db.run(setupAction)

      val f = setupFuture.flatMap { _ =>
        // Insert some movies (using JDBC's batch insert feature)
        val insertAction: DBIO[Option[Int]] = moviesTable ++= Seq(
          (109, true, 111, "pl", 2.33, 3, 96, 7.88, 234, 1993, 22, 12,122, "horror"),
          (101, false, 1, "de", 2.33, 3, 96, 7.88, 234, 1998, 22, 12,122, "comedy")
        )

        val insertAndPrintAction: DBIO[Unit] = insertAction.map { insertResult =>
          // Print the number of rows inserted
          insertResult foreach { numRows =>
            println(s"Inserted $numRows rows into the Movies table")
          }
        }

        val allMoviesAction: DBIO[Seq[(Int, Boolean, Int, String, Double, Int,
          Int, Double, Int, Int, Int, Int, Int, String)]] =
          moviesTable.result

        val combinedAction: DBIO[Seq[(Int, Boolean, Int, String, Double, Int,
          Int, Double, Int, Int, Int, Int, Int, String)]] =
          insertAndPrintAction >> allMoviesAction

        val combinedFuture: Future[Seq[(Int, Boolean, Int, String, Double, Int,
          Int, Double, Int, Int, Int, Int, Int, String)]] =
          db.run(combinedAction)

        combinedFuture.map { allMovies =>
          allMovies.foreach(println)
        }.flatMap { _ =>

          /* Filtering / Where */

          // Construct a query where the price of Coffees is > 9.0
          val filterQuery: Query[MovieData, (Int, Boolean, Int, String, Double, Int,
            Int, Double, Int, Int, Int, Int, Int, String), Seq] =
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
