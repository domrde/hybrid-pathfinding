package client

import common.CommonObjects.InputSettings
import org.scalajs.dom
import org.scalajs.dom.html.Input

import scala.scalajs.js.annotation.JSExport

@JSExport
object InputCollector {

  case class Inputs(calculationTimeout: Input,
                    angleOfSearch: Input,
                    pathStep: Input,
                    deltaToFinish: Input,
                    svmType: List[Input],
                    kernelType: List[Input],
                    gamma: Input,
                    cost: Input,
                    eps: Input)

  var inputs: Inputs = _

  def init() = {
    val svmNodes = dom.document.getElementsByName("svm")
    val svmType: List[Input] = (0 until svmNodes.length).map(i => svmNodes(i)).map(_.asInstanceOf[Input]).toList

    val kernelNodes = dom.document.getElementsByName("kernel")
    val kernelType: List[Input] = (0 until kernelNodes.length).map(i => kernelNodes(i)).map(_.asInstanceOf[Input]).toList

    inputs = Inputs(
      calculationTimeout = dom.document.getElementById("calculationTimeout").asInstanceOf[Input],
      angleOfSearch = dom.document.getElementById("angleOfSearchInput").asInstanceOf[Input],
      pathStep = dom.document.getElementById("pathStepInput").asInstanceOf[Input],
      deltaToFinish = dom.document.getElementById("deltaToFinishInput").asInstanceOf[Input],
      svmType = svmType,
      kernelType = kernelType,
      gamma = dom.document.getElementById("gammaInput").asInstanceOf[Input],
      cost = dom.document.getElementById("costInput").asInstanceOf[Input],
      eps = dom.document.getElementById("epsInput").asInstanceOf[Input]
    )
  }

  val defaultInputSettings = InputSettings(
    calculationTimeout = 1,
    angleOfSearch = 70,
    pathStep = 0.35,
    deltaToFinish = 1.0,
    svmType = 0,
    kernelType = 2,
    gamma = 0.0,
    cost = 11.0,
    eps = -1.0)

  def getCurrentSettings(): InputSettings = {
    val calculationTimeout = inputs.calculationTimeout.value.toInt

    val angleOfSearch = inputs.angleOfSearch.value.toDouble
    val pathStep = inputs.pathStep.value.toDouble
    val deltaToFinish = inputs.deltaToFinish.value.toDouble

    val svmType = inputs.svmType.foldLeft(0) { case (value, input) =>
      if (input.checked) input.value.toInt else value
    }

    val svmKernel = inputs.kernelType.foldLeft(0) { case (value, input) =>
      if (input.checked) input.value.toInt else value
    }

    val gamma = inputs.gamma.value.toDouble
    val cost = inputs.cost.value.toDouble
    val eps = inputs.eps.value.toDouble

    InputSettings(calculationTimeout, angleOfSearch, pathStep, deltaToFinish, svmType, svmKernel, gamma, cost, eps)
  }

  def restoreDefaults(): Unit = {
    inputs.calculationTimeout.value = defaultInputSettings.calculationTimeout.toString
    inputs.angleOfSearch.value = defaultInputSettings.angleOfSearch.toString
    inputs.pathStep.value = defaultInputSettings.pathStep.toString
    inputs.deltaToFinish.value = defaultInputSettings.deltaToFinish.toString

    val groupedSvmType = inputs.svmType.groupBy(_.value.toInt == defaultInputSettings.svmType)
    groupedSvmType.getOrElse(true, List.empty).foreach(_.checked = true)
    groupedSvmType.getOrElse(false, List.empty).foreach(_.checked = false)

    val groupedKernelType = inputs.kernelType.groupBy(_.value.toInt == defaultInputSettings.kernelType)
    groupedKernelType.getOrElse(true, List.empty).foreach(_.checked = true)
    groupedKernelType.getOrElse(false, List.empty).foreach(_.checked = false)

    inputs.gamma.value = defaultInputSettings.gamma.toString
    inputs.cost.value = defaultInputSettings.cost.toString
    inputs.eps.value = defaultInputSettings.eps.toString
  }

}
