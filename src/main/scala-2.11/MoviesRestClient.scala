import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

/**
  * Created by Paulina Sadowska on 01.05.2017.
  */
object MoviesRestClient {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val wsClient = AhcWSClient()

    call(wsClient)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
  }

  def call(wsClient: WSClient): Future[Unit] = {
    wsClient.url("http://jsonplaceholder.typicode.com/comments/1").get().map { response =>
      val body: String = response.body
      println(s"Got a response $body")
    }
  }
}
