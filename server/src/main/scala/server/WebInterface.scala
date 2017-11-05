package server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import common.CommonObjects._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

object Protocols extends DefaultJsonProtocol {
  implicit val pointFormat: RootJsonFormat[Point] = jsonFormat2(Point)
  implicit val obstacleFormat: RootJsonFormat[Obstacle] = jsonFormat3(Obstacle)
  implicit val inputSettingsFormat: RootJsonFormat[InputSettings] = jsonFormat9(InputSettings)
  implicit val configurationFormat: RootJsonFormat[Configuration] = jsonFormat5(Configuration)

  implicit val pointWithAngleFormat: RootJsonFormat[PointWithAngle] = jsonFormat2(PointWithAngle)
  implicit val pathFormat: RootJsonFormat[Path] = jsonFormat2(Path)
  implicit val pathWithAnglesFormat: RootJsonFormat[PathWithAngles] = jsonFormat4(PathWithAngles)
  implicit val exampleFormat: RootJsonFormat[Example] = jsonFormat4(Example)
  implicit val nodeFormat: RootJsonFormat[MapPatch] = jsonFormat4(MapPatch)
  implicit val resultFormat: RootJsonFormat[Result] = jsonFormat4(Result)
}

object WebInterface extends App {
  import Protocols._

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    println("Elapsed time: " + Math.round((t1 - t0) / 1000.0) + " microseconds")
    result
  }

  val routes = {
    get {
      pathSingleSlash {
        getFromResource("index.html")
      } ~ pathPrefix("app") {
        getFromResourceDirectory("app/")
      }
    } ~ post {
      path("pathfind") {
        entity(as[Configuration]) { configuration =>
          print("Calculating... ")
          Try {
            time {
              Pathfinder.findPath(configuration)
            }
          } match {
            case Failure(exception) =>
              println("Failure")
              exception.printStackTrace()
              complete(HttpResponse(StatusCodes.InternalServerError))

            case Success(value) =>
              println("Done")
              complete(value)
          }
        }
      }
    }
  }

  val port = 8080
  val address = "localhost"
  Http().bindAndHandle(routes, address, port)
  println(s"Http service bound to http://$address:$port.")

}
