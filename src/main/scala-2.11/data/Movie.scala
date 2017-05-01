package data

import play.api.libs.json.{Format, JsArray, Json}

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
case class Movie(
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
