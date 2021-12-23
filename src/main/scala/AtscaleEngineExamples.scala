import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.{Flow, Sink}

import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

object AtscaleEngineExamples extends App {
  implicit val system = ActorSystem("LowLevelRest")

  trait AggregateMaintenanceTrigger

  case object PeriodicMaintenance extends AggregateMaintenanceTrigger

  case object TimeBasedMaintenance extends AggregateMaintenanceTrigger

  case class StableProjectContext(orgId: String, projectId: String)

  case class AggregateMaintenanceJob(id: String, t: AggregateMaintenanceTrigger, elements: Set[StableProjectContext])

  var queue = mutable.Queue[AggregateMaintenanceJob](AggregateMaintenanceJob("1", PeriodicMaintenance, Set(StableProjectContext("org1", "project1"))))
  queue.enqueue(AggregateMaintenanceJob("2", PeriodicMaintenance, Set(StableProjectContext("org1", "project1"))))
  queue.enqueue(AggregateMaintenanceJob("3", PeriodicMaintenance, Set(StableProjectContext("org1", "project2"))))
  //  queue.enqueue(AggregateMaintenanceJob("1", PeriodicMaintenance, Set(StableProjectContext("org1", "project1")))) // discard
  //  queue.enqueue(AggregateMaintenanceJob("4", TimeBasedMaintenance, Set(StableProjectContext("org1", "project1"))))
  //  queue.enqueue(AggregateMaintenanceJob("5", PeriodicMaintenance, Set(StableProjectContext("org1", "project1"))))
  //  queue.enqueue(AggregateMaintenanceJob("1", PeriodicMaintenance, Set(StableProjectContext("org1", "project1")))) // discard
  //  queue.enqueue(AggregateMaintenanceJob("4", TimeBasedMaintenance, Set(StableProjectContext("org1", "project1")))) // discard
  //  queue.enqueue(AggregateMaintenanceJob("6", PeriodicMaintenance, Set(StableProjectContext("org2", "project2"))))
  //  queue.enqueue(AggregateMaintenanceJob("3", PeriodicMaintenance, Set(StableProjectContext("org1", "project2")))) // discard

  if (queue.contains(AggregateMaintenanceJob("1", PeriodicMaintenance, Set(StableProjectContext("org1", "project1"))))) {
    // println("The element was discarded")
  } else {
    queue.enqueue(AggregateMaintenanceJob("1", PeriodicMaintenance, Set(StableProjectContext("org1", "project1"))))
  }

  //  val distinctQueue = queue.distinct
  // queue.foreach(println(_))

  case class CubeUpdateMaintenance(udpateType: CubeUpdateType)

  sealed trait CubeUpdateType

  case object FirstTimeCubeProcessed extends CubeUpdateType
  case object StaticCubeUpdated extends CubeUpdateType
  case object DynamicCubeUpdated extends CubeUpdateType

  val cum1 = CubeUpdateMaintenance(StaticCubeUpdated)
  val cum2 = CubeUpdateMaintenance(FirstTimeCubeProcessed)

  val ftcp = FirstTimeCubeProcessed

  // println(ftcp.isInstanceOf[CubeUpdateType])

  //  println(cum1.isInstanceOf[CubeUpdateMaintenance])
  //  println(cum2.isInstanceOf[CubeUpdateMaintenance])

  def removeAt[T](n: Int, xs: List[T]) = {
    xs.slice(0, n) ++ xs.slice(n + 1, xs.length)
  }

  // println(removeAt(1, List('a', 'b', 'c', 'd'))) // List(a, c, d)


  val clq: ConcurrentLinkedQueue[String] = new ConcurrentLinkedQueue()
  clq.add("asd2")
  Option(clq.poll()).foreach{
    case "asd" => println("asd")
    case "ttt" => println("ttt")
  }

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  println("END")

}
