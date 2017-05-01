package data

import play.api.libs.json.{JsError, Json}

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object JsonParser {
  implicit val castReads = Json.reads[Cast]
  implicit val crewReads = Json.reads[Crew]
  implicit val creditsReads = Json.reads[Credits]
  implicit val genreReads = Json.reads[Genre]
  implicit val movieReads = Json.reads[Movie]

  def toMovie(value: String): Movie = {
    val parsed = Json.parse(value)
    val parseResult = parsed.validate[Movie]
    if (parseResult.isError) {
      println("PARSING ERROR:\t" + parseResult.asInstanceOf[JsError].errors.toString())
    }
    movieReads.reads(parsed).get
  }
}
