package server

import common.CommonObjects.{Configuration, MapPatch, Path, Point, distance}

import scala.annotation.tailrec
import scala.util.Random
import scala.util.control.Breaks._

/**
  * Created by dda on 11.05.17.
  */
object AStar {

  def extractStartAndFinish(configuration: Configuration, patches: List[MapPatch]): (MapPatch, MapPatch) = {
    val start = patches.minBy { patch => distance(patch.centroid, configuration.start) }
    val finish = patches.minBy { patch => distance(patch.centroid, configuration.finish) }

    (start, finish)
  }

  def heuristicEstimate(a: MapPatch, b: MapPatch): Double = {
    distance(a.centroid, b.centroid)
  }

  def distanceScore(a: MapPatch, b: MapPatch): Double = {
    distance(a.centroid, b.centroid)
  }

  def reconstructPath(mappedPatches: Map[Int, MapPatch], cameFrom: Map[Int, Int], current: Int, startId: Int): Path = {
    @tailrec
    def reconstructPath(cameFrom: Map[Int, Int], current: Int, path: List[Point]): List[Point] = {
      if (current == startId) mappedPatches(current).centroid :: path
      else reconstructPath(cameFrom - current, cameFrom(current), mappedPatches(current).centroid :: path)
    }

    Path(reconstructPath(cameFrom, current, List.empty),
      s"rgba(${Random.nextInt(127)},${Random.nextInt(127)},${Random.nextInt(127)},0.75)")
  }


  def findPath(configuration: Configuration, patches: List[MapPatch]): Path = {

    val (start, finish) = extractStartAndFinish(configuration, patches)

    val mappedPatches: Map[Int, MapPatch] = patches.map(patch => patch.id -> patch).toMap

    // The set of nodes already evaluated.
    var closedSet: Set[Int] = Set.empty

    // The set of currently discovered nodes that are not evaluated yet.
    // Initially, only the start node is known.
    var openSet: Set[Int] = Set(start.id)

    // For each node, which node it can most efficiently be reached from.
    // If a node can be reached from many nodes, cameFrom will eventually contain the
    // most efficient previous step.
    var cameFrom: Map[Int, Int] = Map.empty

    // For each node, the cost of getting from the start node to that node.
    var gScore: Map[Int, Double] = Map.empty.withDefaultValue(Double.PositiveInfinity)

    // The cost of going from start to start is zero.
    gScore = gScore + (start.id -> 0)

    // For each node, the total cost of getting from the start node to the goal
    // by passing by that node. That value is partly known, partly heuristic.
    var fScore: Map[Int, Double] = Map.empty.withDefaultValue(Double.PositiveInfinity)

    // For the first node, that value is completely heuristic.
    fScore = fScore + (start.id -> heuristicEstimate(start, finish))

    while (openSet.nonEmpty) {
      val current = mappedPatches(openSet.minBy(patch => fScore(patch)))
      if (current == finish) {
        // end
        return reconstructPath(mappedPatches, cameFrom, current.id, start.id)
      }

      openSet = openSet - current.id
      closedSet = closedSet + current.id

      current.exits.foreach { idOfPatch =>
        breakable {
          val neighbour = idOfPatch

          if (closedSet.contains(neighbour)) {
            // Ignore the neighbor which is already evaluated.
            break
          }

          // The distance from start to a neighbor
          val tentativeGScore = gScore(current.id) + distanceScore(current, mappedPatches(neighbour))

          if (!openSet.contains(neighbour)) {
            openSet = openSet + neighbour
          } else if (tentativeGScore >= gScore(neighbour)) {
            // This is not a better path.
            break
          }

          // This path is the best until now. Record it!
          cameFrom = cameFrom + (neighbour -> current.id)
          gScore = gScore + (neighbour -> tentativeGScore)
          fScore = fScore + (neighbour -> (tentativeGScore + heuristicEstimate(mappedPatches(neighbour), finish)))
        }
      }
    }

    // Path not found
    Path(List.empty, "white")
  }

}
