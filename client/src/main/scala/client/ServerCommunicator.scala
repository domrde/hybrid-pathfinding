package client

import client.input.InputCollector
import common.CommonObjects.{Configuration, Result}
import org.scalajs.dom.ext.Ajax

import scala.scalajs.js.annotation.JSExport
import scala.util.Success

@JSExport
class ServerCommunicator(clientSettings: ClientSettings, inputCollector: InputCollector, successfulServerResponseHandler: Result => Unit) {

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  def calculateAndDrawPath(): Unit = {
    val settings = inputCollector.getCurrentSettings()
    val configuration = Configuration(clientSettings.dims, clientSettings.start, clientSettings.finish, clientSettings.obstacles, settings)

    Ajax.post(
      url = "http://localhost:8080/pathfind",
      data = upickle.default.write(configuration),
      timeout = settings.calculationTimeout,
      headers = Map("Content-Type" -> "application/json")
    ).onComplete {
      case Success(value) if value.status == 200 =>
        successfulServerResponseHandler(upickle.default.read[Result](value.responseText))
      case _ =>
    }
  }

}
