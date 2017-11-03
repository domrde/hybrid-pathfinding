package client

import client.Client._
import common.CommonObjects.{Obstacle, Path, PathWithAngles, Point, PointWithAngle, Result, distance}
import org.scalajs.dom
import org.scalajs.dom.html.{Canvas, Div}

import scala.scalajs.js.annotation.JSExport
import scala.util.Random

@JSExport
object CanvasWorker {

  private val backgroundColor = "white"
  private val obstaclesColor = "rgb(48, 110, 105)"
  private val obstaclesBackgroundColor = "rgba(48, 110, 105, 0.25)"
  private val pathColor = "rgb(242, 186, 77)"
  private val smoothPathColor = "brown"
  private val positiveExampleColor = "rgb(252, 121, 79)"
  private val negativeExampleColor = "rgb(63, sbt 81, 101)"

  private var canvas: Canvas = _
  private var context: dom.CanvasRenderingContext2D = _
  var obstacles: List[Obstacle] = _
  var start: Point = _
  var finish: Point = _

  def init() = {
    val canvasCard = dom.document.getElementById("canvasCard").asInstanceOf[Div]
    canvas = dom.document.createElement("canvas").asInstanceOf[Canvas]
    canvasCard.appendChild(canvas)
    canvas.width = canvasCard.clientWidth
    canvas.height = 3 * canvasCard.clientHeight
    context = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    shuffleObstacles()
  }

  def randomStartPoints(obstacles: List[Obstacle]): (Point, Point) = {
    def closeToObstacle(point: Point): Boolean = {
      obstacles.exists(obs => distance(obs, point) < 1.0)
    }

    var start = Utils.randomPoint(dims)
    while (closeToObstacle(start)) {
      start = Utils.randomPoint(dims)
    }

    var finish = Utils.randomPoint(dims)
    while (closeToObstacle(finish) || distance(start, finish) < 4.0) {
      finish = Utils.randomPoint(dims)
    }

    (start, finish)
  }

  def shuffleObstacles(): Unit = {
    obstacles = (1 to (Random.nextInt(10) + 5)).map(_ => Utils.randomPoint(Client.dims))
      .map { case Point(y, x) => Obstacle(y, x, 0.15) }.toList
    val startPoints = randomStartPoints(obstacles)
    start = startPoints._1
    finish = startPoints._2

    redraw()
  }

  def redraw(): Unit = {
    val tileHeight = canvas.height / dims.y
    val tileWidth = canvas.width / dims.x
    clearCanvas()
    redrawObstacles(tileHeight, tileWidth)
    redrawStartAndFinish(tileHeight, tileWidth)
  }

  def draw(result: Result): Unit = {
    val tileHeight = canvas.height / dims.y
    val tileWidth = canvas.width / dims.x
    clearCanvas()
    redrawPatches(tileHeight, tileWidth, result)
    redrawObstacles(tileHeight, tileWidth)
    redrawPaths(tileHeight, tileWidth, result)
    redrawStartAndFinish(tileHeight, tileWidth)
  }

  def clearCanvas(): Unit = {
    context.fillStyle = backgroundColor
    context.fillRect(0, 0, canvas.width, canvas.height)
  }

  def redrawObstacles(tileHeight: Double, tileWidth: Double): Unit = {
    context.strokeStyle = "black"
    context.lineWidth = 1.0

    obstacles.foreach { case Obstacle(y, x, r) =>
      context.beginPath
      context.arc(x * tileWidth, y * tileHeight, r * tileWidth, 0, 2.0 * Math.PI)
      context.stroke
    }
  }

  def redrawPaths(tileHeight: Double, tileWidth: Double, result: Result): Unit = {
    context.lineWidth = 6.0
    redrawPath(result.roughPath, tileWidth, tileHeight, pathColor)

    context.lineWidth = 2.5
    redrawPathWithAngles(result.smoothPath, tileWidth, tileHeight, smoothPathColor)

    result.examples.foreach { example =>
      if (example.c > 0) {
        context.strokeStyle = positiveExampleColor
      } else {
        context.strokeStyle = negativeExampleColor
      }
      context.beginPath()
      context.arc(example.x * tileWidth, example.y * tileHeight, 2, 0, 2 * Math.PI)
      context.stroke()
    }
  }

  def redrawPathWithAngles(path: PathWithAngles, tileWidth: Double, tileHeight: Double, color: String): Unit = {
    if (path.path.nonEmpty) {
      context.strokeStyle = color
      context.beginPath()
      context.moveTo(path.path.head.p.x * tileWidth, path.path.head.p.y * tileHeight)
      path.path.tail.foreach { case PointWithAngle(Point(y, x), angle) =>
        context.lineTo(x * tileWidth, y * tileHeight)
      }
      context.stroke()

      val pathStep = InputCollector.inputs.pathStep.value.toDouble
      val angleOfSearch = InputCollector.inputs.angleOfSearch.value.toDouble
      path.path.foreach { case PointWithAngle(Point(y, x), angle) =>
        //        context.fillStyle = "black"
        //        context.fillCircle(x * tileWidth, y * tileHeight, 3.0)

        context.fillStyle = "rgba(139,69,19,0.25)"
        context.beginPath()
        context.moveTo(x * tileWidth, y * tileHeight)
        context.arc(x * tileWidth, y * tileHeight, pathStep * tileWidth,
          Math.toRadians(angle - angleOfSearch), Math.toRadians(angle + angleOfSearch))
        context.fill()
      }
    }
  }

  def redrawPath(path: Path, tileWidth: Double, tileHeight: Double, color: String): Unit = {
    if (path.path.nonEmpty) {
      context.strokeStyle = color
      context.beginPath()
      context.moveTo(path.path.head.x * tileWidth, path.path.head.y * tileHeight)
      path.path.tail.foreach { case Point(y, x) =>
        context.lineTo(x * tileWidth, y * tileHeight)
      }
      context.stroke()

      //      context.fillStyle = "black"
      //      path.path.foreach { case Point(y, x) =>
      //        context.fillCircle(x * tileWidth, y * tileHeight, 3.0)
      //      }
    }
  }

  def redrawPatches(tileHeight: Double, tileWidth: Double, result: Result) = {
    result.patches.foreach { patch =>
      context.strokeStyle = obstaclesColor
      context.fillStyle = obstaclesBackgroundColor
      context.lineWidth = 0.5
      val patchPoints = patch.coordinates
      context.beginPath()
      context.moveTo(patchPoints.head.x * tileWidth, patchPoints.head.y * tileHeight)
      patchPoints.tail.foreach { case Point(y, x) => context.lineTo(x * tileWidth, y * tileHeight) }
      context.closePath()
      context.stroke()
      context.fill()
    }
  }

  def redrawStartAndFinish(tileHeight: Double, tileWidth: Double) = {
    context.strokeStyle = "black"
    context.lineWidth = 2.0
    context.font = "50px Arial Black"
    context.strokeText("x", start.x * tileHeight - 15, start.y * tileWidth + 10)
    context.strokeText("x", finish.x * tileHeight - 15, finish.y * tileWidth + 10)
  }

}
