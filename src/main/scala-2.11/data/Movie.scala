package data

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
case class Movie(
                  id: Int,
                  adult: Boolean,
                  budget: Int,
                  private val genres: Seq[Genre],
                  original_language: String,
                  popularity: Double,
                  private val release_date: String,
                  revenue: Int,
                  runtime: Int,
                  vote_average: Double,
                  vote_count: Int,
                  private val credits: Credits
                ) {

  val genreIds = getGenres(genres)
  val releaseDate = getReleaseYear(release_date)
  val directorId = findCrewByJob(credits, "Director")
  val firstActorId = findActorIdByOrder(credits, 0)
  val secondActorId = findActorIdByOrder(credits, 1)

  def getGenres(genres: Seq[Genre]): Seq[Int] = {
    genres.map({
      _.id
    })
  }

  def getReleaseYear(release_date: String) = {
    release_date.split("-")(0)
  }

  def findCrewByJob(credits: Credits, job: String): Int = {
    credits.crew.filter(_.job == job).head.id
  }

  def findActorIdByOrder(credits: Credits, order: Int) = {
    credits.cast.filter(_.order == order).head.id
  }
}
