import akka.actor.ActorSystem
import akka.stream.OverflowStrategy.fail
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

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

object SourceSinkTest extends App {
  implicit val system = ActorSystem("LowLevelRest")

  val slowSink = Sink.foreach[Weather] { x =>
    // simulate a long processing
    Thread.sleep(2000)
    println(s"Sink: $x")
  }
  val removeConsecutiveDuplicates = Flow[Weather]
    .sliding(2, 1)
    .mapConcat { case prev +: current +: _=>
      if (prev == current) Nil
      else List(current)
    }
    .map { x =>
      println(s"Incoming: $x")
      x
    }

//  val flow = Flow[Int]
//    .sliding(2, 1)
//    .mapConcat { case prev +: current +: _=>
//      if (prev == current) Nil
//      else List(current)
//    }
  case class Weather(zipCode : String, temperature : Double, raining : Boolean)

  val bufferSize = 1000
  //if the buffer fills up then this strategy drops the oldest elements
  //upon the arrival of a new element.
  val overflowStrategy = akka.stream.OverflowStrategy.backpressure

  val queue = Source.queue[Weather](bufferSize, overflowStrategy)
    .filter(_.raining)
    .via(removeConsecutiveDuplicates)
    .to(slowSink)
    .run() // in order to "keep" the queue Materialized value instead of the Sink's

  queue offer Weather("111", 35.0, true)
  queue offer Weather("111", 35.0, true)
  queue offer Weather("133", 36.0, false)
  queue offer Weather("111", 35.0, true)
  queue offer Weather("155", 35.0, true)
  queue offer Weather("111", 35.0, true)
  queue offer Weather("177", 35.0, true)
  queue offer Weather("188", 35.0, true)
  queue offer Weather("199", 35.0, true)

//  val ref = Source.actorRef[Weather](Int.MaxValue, fail)
//    .filter(!_.raining)
//    .to(Sink foreach println )
//    .run() // in order to "keep" the ref Materialized value instead of the Sink's
//
//  ref ! Weather("211", 32.0, true)
//  ref ! Weather("222", 32.0, false)
//  ref ! Weather("233", 32.0, false)

//  val queue2 = Source
//    .queue[Int](bufferSize)
//    //.map(x => x * x)
//    .toMat(Sink.foreach(x => println(s"completed $x")))(Keep.left)
//    .run()
//
//  val fastElements = 1 to 10
//
//  println(fastElements)
//  implicit val ec = system.dispatcher
//  fastElements.foreach { x =>
//    queue2.offer(x) match {
//      case QueueOfferResult.Enqueued    => println(s"enqueued $x")
//      case QueueOfferResult.Dropped     => println(s"dropped $x")
//      case QueueOfferResult.Failure(ex) => println(s"Offer failed ${ex.getMessage}")
//      case QueueOfferResult.QueueClosed => println("Source Queue closed")
//    }
//  }
}
