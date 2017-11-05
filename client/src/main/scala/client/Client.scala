package client

import client.input.InputCollector
import common.CommonObjects._
import org.scalajs.dom
import org.scalajs.dom.html.Input

import scala.scalajs.js.annotation.JSExport

@JSExport
object Client {

  dom.window.onload = _ => {
    val clientSettings = new ClientSettings()
    val canvasWorker = new CanvasWorker(clientSettings)
    val inputCollector = new InputCollector()

    val successfulServerResponseHandler =
      (result: Result) => canvasWorker.draw(result)

    val serverCommunicator = new ServerCommunicator(clientSettings, inputCollector, successfulServerResponseHandler)

    dom.document.getElementById("restoreDefaults").asInstanceOf[Input].onmousedown =
      (e: dom.raw.Event) => inputCollector.restoreDefaults()

    dom.document.getElementById("placeObstacles").asInstanceOf[Input].onmousedown =
      (e: dom.raw.Event) => { clientSettings.shuffleObstacles(); canvasWorker.redraw() }

    dom.document.getElementById("submit").asInstanceOf[Input].onmousedown =
      (e: dom.raw.Event) => serverCommunicator.requestPathCalculation()

    canvasWorker.redraw()
  }

  @JSExport
  def main(): Unit = {
  }
}
