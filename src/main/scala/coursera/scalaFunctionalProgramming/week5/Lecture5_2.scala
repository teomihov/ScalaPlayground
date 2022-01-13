package coursera.scalaFunctionalProgramming.week5

import scala.::

object Lecture5_2 extends App {
  def msort(xs: List[Int]): List[Int] = {
    val n = xs.length / 2
    if (n == 0) xs
    else {
      val (fst, snd) = xs splitAt n
      println(s"First: $fst; second: $snd")
      merge1(msort(fst), msort(snd))
    }
  }

  def merge(xs: List[Int], ys: List[Int]): List[Int] =
    xs match {
      case Nil =>
        ys
      case xHead :: xTail =>
        ys match {
          case Nil =>
            xs
          case yHead :: yTail =>
            if (xHead < yHead) xHead :: merge(xTail, ys)
            else yHead :: merge(yTail, xs)
        }
    }

  def merge1(xs: List[Int], ys: List[Int]): List[Int] = {
    println("Merge xs: " + xs + " ys: " + ys)
    (xs, ys) match {
      case (Nil, Nil) => List.empty
      case (xList, Nil) => xList
      case (Nil, yList) => yList
      case (xHead :: xTail, yHead :: yTail) =>
        if (xHead < yHead) xHead :: merge1(xTail, ys)
        else yHead :: merge1(yTail, xs)
    }
  }

  val nums = List(2, -4, 5, 7, 1)
  println(msort(nums))

  def msortGeneric[T](xs: List[T])(lt: (T, T) => Boolean): List[T] = {
    val n = xs.length / 2
    if (n == 0) xs
    else {
      val (fst, snd) = xs splitAt n
      mergeGeneric(msortGeneric(fst)(lt), msortGeneric(snd)(lt))(lt)
    }
  }

  def mergeGeneric[T](xs: List[T], ys: List[T])(lt: (T, T) => Boolean): List[T] = {
    (xs, ys) match {
      case (Nil, Nil) => List.empty
      case (xList, Nil) => xList
      case (Nil, yList) => yList
      case (xHead :: xTail, yHead :: yTail) =>
        if (lt(xHead, yHead)) xHead :: mergeGeneric(xTail, ys)(lt)
        else yHead :: mergeGeneric(yTail, xs)(lt)
    }
  }

  val fruits = List("apple", "orange", "tomato", "banana", "kiwi")
  println(msortGeneric(fruits)((x: String, y: String) => x < y))
}
