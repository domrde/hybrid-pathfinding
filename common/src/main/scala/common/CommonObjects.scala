package common

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object CommonObjects {
  implicit def obstacleToPoint(a: Obstacle): Point = Point(a.y, a.x)

  def distance(p1: Point, p2: Point): Double = Math.sqrt(Math.pow(p2.x - p1.x, 2.0) + Math.pow(p2.y - p1.y, 2.0))

  def pointInBetween(a: Point, b: Point, t: Double): Point = Point((1.0 - t) * a.y + t * b.y, (1.0 - t) * a.x + t * b.x)

  @JSExportAll
  case class Obstacle(y: Double, x: Double, r: Double) {
    def isInside(p: Point): Boolean = {
      Math.pow(p.y - y, 2.0) + Math.pow(p.x - x, 2.0) <= Math.pow(r + 0.5, 2.0)
    }
  }

  @JSExportAll
  case class Example(y: Double, x: Double, c: Double, r: Double)

  @JSExportAll
  case class Point(var y: Double, var x: Double) {
    def update(p: Point): Unit = {
      x = p.x; y = p.y
    }
  }

  @JSExportAll
  case class PointWithAngle(p: Point, angle: Double)

  @JSExportAll
  case class Path(path: List[Point], color: String)

  @JSExportAll
  case class PathWithAngles(path: List[PointWithAngle], color: String, pathStep: Double, angleOfSearch: Double)

  @JSExportAll
  case class MapPatch(var id: Int, coordinates: List[Point], centroid: Point, var exits: Set[Int] = Set.empty) {
    def addExit(node: MapPatch): Unit = exits = exits + node.id
  }

  @JSExportAll
  case class InputSettings(calculationTimeout: Int, angleOfSearch: Double, pathStep: Double,
                           deltaToFinish: Double, svmType: Int, kernelType: Int, gamma: Double,
                           cost: Double, eps: Double)

  @JSExportAll
  case class Configuration(dims: Point, start: Point, finish: Point, obstacles: List[Obstacle], settings: InputSettings)

  @JSExportAll
  case class Result(smoothPath: PathWithAngles, roughPath: Path, patches: List[MapPatch], examples: List[Example])

}
