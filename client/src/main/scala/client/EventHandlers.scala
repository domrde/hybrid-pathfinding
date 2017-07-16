package client

import org.scalajs.dom
import org.scalajs.dom.html.Input

import scala.scalajs.js.annotation.JSExport

@JSExport
object EventHandlers {
  def setHandlers() = {
    dom.document.getElementById("restoreDefaults").asInstanceOf[Input].onmousedown = (e: dom.raw.Event) => InputCollector.restoreDefaults()

    dom.document.getElementById("placeObstacles").asInstanceOf[Input].onmousedown = (e: dom.raw.Event) => CanvasWorker.shuffleObstacles()

    dom.document.getElementById("submit").asInstanceOf[Input].onmousedown = (e: dom.raw.Event) => Client.calculateAndDrawPath()
  }
}
