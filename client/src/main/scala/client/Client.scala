package client

import common.CommonObjects._
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExport
import scala.util.Success

@JSExport
object Client {
  import dom.ext._

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  val dims = Point(10.0, 20.0)

  dom.window.onload = _ => {
    CanvasWorker.init()
    EventHandlers.setHandlers()
    InputCollector.init()
    CanvasWorker.draw()
  }

  @JSExport
  def main(): Unit = {
  }

  def calculateAndDrawPath(): Unit = {
    println("Sending data")

    Ajax.post(
      url = "http://localhost:8080/pathfind",
      data = upickle.default.write(Configuration(dims, CanvasWorker.start, CanvasWorker.finish, CanvasWorker.obstacles,
        InputCollector.getCurrentSettings())),
      headers = Map("Content-Type" -> "application/json")
    ).onComplete {
      case Success(value) if value.status == 200 =>
        CanvasWorker.draw(upickle.default.read[Result](value.responseText))
      case _ =>
    }
  }
}
