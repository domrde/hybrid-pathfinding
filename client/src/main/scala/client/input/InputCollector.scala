package client.input

import common.CommonObjects.InputSettings
import org.scalajs.dom
import org.scalajs.dom.Event

import scala.scalajs.js.annotation.JSExport

@JSExport
class InputCollector {

  val inputs = new Inputs()

  val defaultInputSettings = InputSettings(
    calculationTimeout = 1000,
    angleOfSearch = 70,
    pathStep = 0.35,
    deltaToFinish = 1.0,
    svmType = 0,
    kernelType = 2,
    gamma = 0.0,
    cost = 11.0,
    eps = -1.0
  )

  def getCurrentSettings(): InputSettings = {
    val calculationTimeout = inputs.calculationTimeout.value.toInt

    val angleOfSearch = inputs.angleOfSearch.value.toDouble
    val pathStep = inputs.pathStep.value.toDouble
    val deltaToFinish = inputs.deltaToFinish.value.toDouble

    val gamma = 2.0 * Math.exp(inputs.gamma.value.toDouble)
    val cost = 2.0 * Math.exp(inputs.cost.value.toDouble)
    val eps = 2.0 * Math.exp(inputs.eps.value.toDouble)

    val svmType = inputs.svmType.value.toInt
    val svmKernel = inputs.kernelType.value.toInt

    InputSettings(calculationTimeout, angleOfSearch, pathStep, deltaToFinish, svmType, svmKernel, gamma, cost, eps)
  }

  def restoreDefaults(): Unit = {
    inputs.calculationTimeout.value = defaultInputSettings.calculationTimeout.toString

    inputs.angleOfSearch.value = defaultInputSettings.angleOfSearch.toString
    inputs.pathStep.value = defaultInputSettings.pathStep.toString
    inputs.deltaToFinish.value = defaultInputSettings.deltaToFinish.toString

    inputs.gamma.value = defaultInputSettings.gamma.toString
    inputs.cost.value = defaultInputSettings.cost.toString
    inputs.eps.value = defaultInputSettings.eps.toString

    inputs.svmType.value = defaultInputSettings.svmType.toString
    inputs.kernelType.value = defaultInputSettings.kernelType.toString

    inputs.inputs.foreach { input =>
      val customEvent = dom.document.createEvent("Event")
      customEvent.initEvent("input", canBubbleArg = true, cancelableArg = true)
      input.dispatchEvent(customEvent)
    }
  }

}
