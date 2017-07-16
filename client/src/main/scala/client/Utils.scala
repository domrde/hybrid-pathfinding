package client

import common.CommonObjects.Point

import scala.scalajs.js.annotation.JSExport
import scala.util.Random

@JSExport
object Utils {

  def randomPoint(dims: Point): Point = {
    Point(
      Random.nextInt((dims.y.toInt - 1) * 10).toDouble / 10.0,
      Random.nextInt((dims.x.toInt - 1) * 10).toDouble / 10.0
    )
  }

}
