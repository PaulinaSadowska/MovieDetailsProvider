package data

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
case class Movie(
                  id: Int,
                  adult: Boolean,
                  budget: Int,
                  genres: Seq[Genre],
                  original_language: String,
                  popularity: Double,
                  release_date: String,
                  revenue: Int,
                  runtime: Int,
                  vote_average: Double,
                  vote_count: Int,
                  credits: Credits
                ) {
}
