package coursera.scalaFunctionalProgramming.week5

import scala.::

object MoreFunctions extends App {
  def concat[T](xs: List[T], ys: List[T]): List[T] = xs match {
    case List() => ys
    case h :: restElements => h :: concat(restElements, ys)
  }

  println(s"Concat: ${concat(List(1,2,3), List(3,4,5))}")

  def reverse[T](xs: List[T]): List[T] = xs match {
    case List() => List()
    case h :: restElements => reverse(restElements) ++ List(h)
  }

  def removeAtMy[T](n: Int, xs: List[T]) = {
    xs.slice(0, n) ++ xs.slice(n + 1, xs.length)
  }

  def removeAt[T](n: Int, xs: List[T]) = {
    (xs.take(n)) ::: (xs.drop(n + 1))
  }

  def flatten(xs: List[Any]): List[Any] = xs match {
    case List() => List()
    case (h: List[Any]) :: tail => flatten(h) ++ flatten(tail)
    case h :: tail => h :: flatten(tail)
  }

  println(flatten(List(List(1, 1), 2, List(3, List(5, 8)))))
}

