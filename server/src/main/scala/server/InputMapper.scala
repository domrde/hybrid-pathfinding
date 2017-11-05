package server

import common.CommonObjects.Point

object InputMapper {

  def distance(p1: Point, p2: Point): Double = Math.sqrt(Math.pow(p2.x - p1.x, 2.0) + Math.pow(p2.y - p1.y, 2.0))

  def pointInBetween(a: Point, b: Point)(t: Double): Point = Point((1.0 - t) * a.y + t * b.y, (1.0 - t) * a.x + t * b.x)

  def getPivotPoints(start: Point, finish: Point, pointOfInterest: Point, l: Double): (Point, Point) = {
    def solve(a: Double, b: Double, c: Double): (Double, Double) = {
      val d = Math.pow(b, 2.0) - 4 * a * c
      ((-b + Math.sqrt(d)) / 2.0 / a, (-b - Math.sqrt(d)) / 2.0 / a)
    }

    def calculateNeighbourPoints(point: Point, m: Double, b: Double): (Point, Point) = {
      val (first, second) = solve(
        1 + Math.pow(m, 2),
        2 * m * (b - point.y) - 2 * point.x,
        Math.pow(point.x, 2) + Math.pow(b - point.y, 2) - Math.pow(l, 2)
      )

      (Point(m * first + b, first), Point(m * second + b, second))
    }

    val m = (finish.y - start.y + 1e-6) / (finish.x - start.x + 1e-6)
    val mp = -1 / m

    calculateNeighbourPoints(pointOfInterest, mp, pointOfInterest.y - mp * pointOfInterest.x)
  }


}


