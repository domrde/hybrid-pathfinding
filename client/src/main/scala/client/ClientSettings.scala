package client

import common.CommonObjects.{Obstacle, Point, distance}

import scala.scalajs.js.annotation.JSExport
import scala.util.Random

@JSExport
class ClientSettings {

  def dims = Point(10.0, 20.0)

  var obstacles: List[Obstacle] = _
  var start: Point = _
  var finish: Point = _

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
    obstacles = (1 to (Random.nextInt(10) + 5)).map(_ => Utils.randomPoint(dims))
      .map { case Point(y, x) => Obstacle(y, x, 0.15) }.toList
    val startPoints = randomStartPoints(obstacles)
    start = startPoints._1
    finish = startPoints._2
  }

  shuffleObstacles()

}
