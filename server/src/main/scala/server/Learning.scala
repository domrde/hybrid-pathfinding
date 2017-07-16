package server

import java.util.concurrent.Executors

import libsvm._
import libsvm.svm_parameter._
import common.CommonObjects
import common.CommonObjects._

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object Learning {

  implicit val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(200))

  case class RunResults(paths: List[List[PointWithAngle]], message: String)

  svm.svm_set_print_string_function(new svm_print_interface {
    override def print(s: String): Unit = {
      // do nothing
    }
  })

  private def trainSvmModel(svmType: Int, kernel: Int, field: List[Example], gamma: Double, cost: Double, eps: Double) = {
    val prob = new svm_problem
    prob.l = field.size
    prob.y = new Array[Double](prob.l)

    prob.x = Array.fill(prob.l, 2)(new svm_node)
    field.indices.foreach { i =>
      val p = field(i)
      prob.x(i)(0).index = 1
      prob.x(i)(0).value = p.x
      prob.x(i)(1).index = 2
      prob.x(i)(1).value = p.y
      prob.y(i) = p.c
    }

    val param = new svm_parameter
    param.svm_type = svmType
    param.kernel_type = kernel
    param.degree = 3
    param.gamma = gamma
    param.coef0 = 0
    param.nu = 0.5
    param.cache_size = 40
    param.C = cost
    param.eps = eps
    param.p = 0.1
    param.shrinking = 0
    param.probability = 0
    param.nr_weight = 0
    param.weight_label = new Array[Int](0)
    param.weight = new Array[Double](0)

    svm.svm_train(prob, param)
  }

  private def buildPath(model: svm_model, d: Double, angleDelta: Double, from: Point, to: Point, dims: Point, deltaToFinish: Double): List[List[PointWithAngle]] = {
    val par = Array.fill(2)(new svm_node)
    par(0) = new svm_node
    par(1) = new svm_node
    par(0).index = 1
    par(1).index = 2

    def findAngleOfSignChange(curPoint: Point, initialAngle: Double, toAngle: Double,
                              step: Double, distance: Double): Option[Double] = {
      val par = Array.fill(2)(new svm_node)
      par(0) = new svm_node
      par(1) = new svm_node
      par(0).index = 1
      par(1).index = 2

      val y = Math.sin(Math.toRadians(initialAngle)) * distance
      val x = Math.cos(Math.toRadians(initialAngle)) * distance
      par(0).value = (curPoint.x + x) / dims.x
      par(1).value = (curPoint.y + y) / dims.y
      val initialGreaterThanZero = svm.svm_predict(model, par) > 0

      @tailrec
      def rec(curAngle: Double): Option[Double] = {
        if (Math.abs(curAngle - toAngle) < step) None
        else {
          val y = Math.sin(Math.toRadians(curAngle)) * distance
          val x = Math.cos(Math.toRadians(curAngle)) * distance
          par(0).value = (curPoint.x + x) / dims.x
          par(1).value = (curPoint.y + y) / dims.y
          val curSignGreaterThanZero = svm.svm_predict(model, par) > 0
          if (curSignGreaterThanZero == initialGreaterThanZero) {
            rec(curAngle + step)
          } else {
            Some(curAngle)
          }
        }
      }

      rec(initialAngle + step)
    }

    def findNearestPointsOfPath(source: Point): List[Point] = {

      @tailrec
      def rec(distance: Double): List[Point] = {
        if (distance > dims.y * 0.55) {
          List.empty
        }
        else {
          val angleOfFirstPoint = findAngleOfSignChange(source, -180, 180, 20, distance)
          if (angleOfFirstPoint.isDefined) {
            val angleOfSecondPoint = findAngleOfSignChange(source, angleOfFirstPoint.get + 20, 180, 20, distance)
            if (angleOfSecondPoint.isDefined) {
              List(
                Point(
                  source.y + Math.sin(Math.toRadians(angleOfFirstPoint.get)) * distance,
                  source.x + Math.cos(Math.toRadians(angleOfFirstPoint.get)) * distance
                ),
                Point(
                  source.y + Math.sin(Math.toRadians(angleOfSecondPoint.get)) * distance,
                  source.x + Math.cos(Math.toRadians(angleOfSecondPoint.get)) * distance
                )
              )
            } else rec(distance + d)
          } else rec(distance + d)
        }
      }

      rec(d)
    }


    def buildPath(accumulator: List[PointWithAngle], limit: Int): List[PointWithAngle] = {
      val curPoint = accumulator.head.p
      val curAngle = accumulator.head.angle
      if (limit < 0 || distance(to, curPoint) < deltaToFinish) {
        accumulator
      } else {
        val estimateAngleOfSignChange =
          findAngleOfSignChange(curPoint, curAngle - angleDelta, curAngle + angleDelta, 10, d).getOrElse(curAngle)

        val specificAngleOfSignChange =
          findAngleOfSignChange(curPoint, estimateAngleOfSignChange - 10, estimateAngleOfSignChange + 10, 1, d).getOrElse(curAngle)

        val y = Math.sin(Math.toRadians(specificAngleOfSignChange)) * d
        val x = Math.cos(Math.toRadians(specificAngleOfSignChange)) * d
        par(0).value = (curPoint.x + x) / dims.x
        par(1).value = (curPoint.y + y) / dims.y
        Point(curPoint.y + y, curPoint.x + x)

        buildPath(PointWithAngle(Point(curPoint.y + y, curPoint.x + x), specificAngleOfSignChange) :: accumulator, limit - 1)
      }
    }

    findNearestPointsOfPath(from).flatMap { first =>
      findNearestPointsOfPath(first).map { second =>
        val angle = Math.toDegrees(Math.atan2(first.y - second.y, first.x - second.x))
        val firstWithAngle = PointWithAngle(first, angle)
        val secondWithAngle = PointWithAngle(second, angle)
        buildPath(List(firstWithAngle, secondWithAngle), 150).reverse
      }
    }
  }

  private def checkPathCorrect(examples: List[Example], roughPath: List[Point],
                               config: Configuration, eps: Double)(path: PathWithAngles): Boolean = {
    val smoothPath = path.path

    val pointOutsideField = smoothPath.exists { case PointWithAngle(point, _) =>
      point.x < 0 || point.y < 0 || point.x > config.dims.x || point.y > config.dims.y }

    val isAwayFromStart = distance(smoothPath.head.p, config.start) > eps && distance(smoothPath.last.p, config.start) > eps

    val isAwayFromFinish = distance(smoothPath.head.p, config.finish) > eps && distance(smoothPath.last.p, config.finish) > eps

    val isCorrect = !pointOutsideField

    isCorrect
  }

  private def runSVM(obstacles: List[Example], dims: Point, start: Point, finish: Point, pathStep: Double, angleOfSearch: Double, deltaToFinish: Double): List[Future[RunResults]] = {

    val radialModels =
      List(2e-1).map { eps =>
        Future {
          trainSvmModel(svmType = C_SVC, kernel = RBF, field = obstacles, gamma = 1, cost = 2e11, eps = eps)
        }
      }

    radialModels.map { modelFuture =>
      modelFuture.map { model =>
        RunResults(buildPath(model, pathStep, angleOfSearch / 2.0, start, finish, dims, deltaToFinish), "")
      }
    }
  }

  def smoothPath(configuration: Configuration, roughPath: Path): (PathWithAngles, List[Example]) = {
    if (roughPath.path.size < 2) {
      (PathWithAngles(List.empty, "black"), List.empty)
    } else {
      val height = configuration.dims.y
      val width = configuration.dims.x
      val minDim = Math.min(width, height)
      val distanceOfExampleFromRoughPath = minDim * 0.06
      val stepOfExampleGeneration = minDim * 0.01

      def classify(s: Point, f: Point, target: Point) = {
        if ((f.x - s.x) * (target.y - s.y) < (f.y - s.y) * (target.x - s.x)) 1.0 else -1.0
      }

      val examples = roughPath.path.sliding(2).flatMap {
        case a :: b :: Nil =>
          val part = stepOfExampleGeneration / CommonObjects.distance(a, b) + 0.01
          (0.25 to 0.75 by part)
            .map(t => pointInBetween(a, b, t))
            .flatMap { p =>
              (1.0 to 3.0 by 0.3).map(c => InputMapper.getPivotPoints(a, b, p, c * distanceOfExampleFromRoughPath))
            }
            .flatMap { case (Point(ay, ax), Point(by, bx)) => List(Point(ay, ax), Point(by, bx)) }
            .map { p => Example(p.y, p.x, classify(a, b, p), 0.15) }

        case other =>
          throw new RuntimeException("Illegal list sequence in Learning: " + other)
      }.toList

      val inRanges =
        examples
          .filterNot { case Example(y, x, _, _) =>
            roughPath.path.exists(p => distance(Point(y, x), p) < distanceOfExampleFromRoughPath)
          }

      val normalized = inRanges.map { case Example(y, x, c, r) =>
        Example(y / height, x / width, c, r)
      }

      val results = Learning.runSVM(normalized, configuration.dims, configuration.start, configuration.finish,
        configuration.settings.pathStep, configuration.settings.angleOfSearch, configuration.settings.deltaToFinish)

      val paths =
        Await.result(Future.sequence(results), 220.second)
          .filter(_.paths.nonEmpty)
          .flatMap { runResults =>
            runResults.paths.map { points =>
              PathWithAngles(points, s"orange")
            }
          }
          .filter(checkPathCorrect(inRanges, roughPath.path, configuration, 3.0))

      val path =
        if (paths.nonEmpty) {
          paths.minBy(path => path.path.sliding(2).map { case a :: b :: Nil => distance(a.p, b.p) }.sum)
        } else PathWithAngles(List.empty, "white")

      (path, inRanges)
    }
  }
}