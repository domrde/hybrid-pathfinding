package client.input

import org.scalajs.dom
import org.scalajs.dom.html.{Input, Select}

import scala.scalajs.js.annotation.JSExport

@JSExport
class Inputs {

  def calculationTimeout: Input = dom.document.getElementById("calculationTimeout").asInstanceOf[Input]

  // Path restoration
  def angleOfSearch: Input = dom.document.getElementById("angleOfSearchInput").asInstanceOf[Input]
  def pathStep: Input = dom.document.getElementById("pathStepInput").asInstanceOf[Input]
  def deltaToFinish: Input = dom.document.getElementById("deltaToFinishInput").asInstanceOf[Input]

  // SVM settings
  def svmType: Select = dom.document.getElementById("svmType").asInstanceOf[Select]
  def kernelType: Select = dom.document.getElementById("kernel").asInstanceOf[Select]

  def gamma: Input = dom.document.getElementById("gammaInput").asInstanceOf[Input]
  def cost: Input = dom.document.getElementById("costInput").asInstanceOf[Input]
  def eps: Input = dom.document.getElementById("epsInput").asInstanceOf[Input]

  def inputs: List[Input] = List(calculationTimeout, angleOfSearch, pathStep, deltaToFinish, gamma, cost, eps)
}
