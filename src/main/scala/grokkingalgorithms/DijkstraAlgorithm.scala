package grokkingalgorithms

import scala.annotation.tailrec
import scala.collection.mutable.Map

object DijkstraAlgorithm extends App {
  val graph = Map(
    "start" -> Map("a" -> 6, "b" -> 2),
    "a" -> Map("fin" -> 1),
    "b" -> Map("a" -> 3, "fin" -> 5),
  )
  val costs = Map(
    "a" -> 6,
    "b" -> 2,
    "fin" -> Int.MaxValue
  )

  val parents = Map(
    "a" -> "start",
    "b" -> "start",
    "fin" -> "none",
  )
  var alreadyProceed: List[String] = List.empty[String]

  def findLowestCostNode(costs: Map[String, Int]): String= {
    @tailrec
    def findLowestHelper(costs: Map[String, Int], currentLowest: String, currentLowestValue: Int):String = {
      if(costs.isEmpty) currentLowest
      else if(currentLowestValue > costs.head._2 && !alreadyProceed.contains(costs.head._1)) findLowestHelper(costs.tail, costs.head._1, costs.head._2)
      else findLowestHelper(costs.tail, currentLowest, currentLowestValue)
    }

    findLowestHelper(costs.tail, costs.head._1, costs.head._2)
  }

  def proceedNodes(): Unit = {
    val node = findLowestCostNode(costs)
    if(node == "fin") return

    val cost = costs(node)
    val neighbours = graph(node)

    for (n <- neighbours) {
        updateWithSmallestCost(node, n, cost)
    }

    alreadyProceed = alreadyProceed :+ node

    proceedNodes()
  }

  def updateWithSmallestCost(currentNode: String, neighbour: (String,Int), cost: Int) = {
    val newCost = neighbour._2 + cost
    if(newCost < costs(neighbour._1)) {
      costs(neighbour._1) = newCost
      parents(neighbour._1) = currentNode
    }
  }

  proceedNodes()
  println(costs)
  println(parents)
}
