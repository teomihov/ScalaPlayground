package grokkingalgorithms

import scala.collection.mutable

object BreadthFirstSearch extends App {
  def bfs(graph: Map[String, List[String]],find: String => Boolean): String = {
    def bfsHelper(graph: Map[String, List[String]], queue: mutable.Queue[String], alreadySeen: List[String]): String = {
      if(queue.isEmpty) ""
      else {
        val user = queue.dequeue()
        if(find(user)) user
        else if(alreadySeen.contains(user)) bfsHelper(graph, queue, alreadySeen)
        else {
          directedGraph(user).foreach(x => queue.enqueue(x))
          bfsHelper(graph, queue, alreadySeen :+ user)
        }
      }
    }

    val initialQueueItems = mutable.Queue.empty[String]
    directedGraph("you").foreach(x => initialQueueItems.enqueue(x))
    bfsHelper(graph, initialQueueItems, List.empty[String])
  }

  def isThisCorrectUser(name: String) = if(name.endsWith("m")) true else false

  val directedGraph = Map(
    "you" -> List("alice", "bob", "claire"),
    "bob" -> List("anuj", "peggy"),
    "alice" -> List("peggy"),
    "claire" -> List("tom", "jonny"),
    "anuj" -> List.empty,
    "peggy" -> List.empty,
    "tom" -> List.empty,
    "jonny" -> List.empty,
  )

  println("Result: " + bfs(directedGraph, isThisCorrectUser))
}
