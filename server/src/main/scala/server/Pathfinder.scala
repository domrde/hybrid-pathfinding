package server

import common.CommonObjects.{Configuration, MapPatch, Path, Result}

/**
  * Created by dda on 11.05.17.
  */
object Pathfinder {
  def findPath(configuration: Configuration): Result = {
    val patches: List[MapPatch] = Patcher.preparePatches(configuration)
    val roughPath: Path = AStar.findPath(configuration, patches)
    val (smoothPath, examples) = Learning.smoothPath(configuration, roughPath)
    Result(smoothPath, roughPath, patches, examples)
  }
}
