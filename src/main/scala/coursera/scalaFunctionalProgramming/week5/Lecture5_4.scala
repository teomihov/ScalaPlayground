package coursera.scalaFunctionalProgramming.week5

object Lecture5_4 extends App {
//  abstract class List[T] {
//    def map[U](f: T => U): List[U] = this match {
//      case Nil => this
//      case x :: xs => f(x) :: xs map f
//    }
//    def filter(p: T => Boolean): List[T] = this match {
//      case Nil => Nil
//      case x :: xs => if(p(x)) x :: xs.filter(p) else xs.filter(p)
//    }
//  }

  val nums = List(2, -4, 5, 7, 1)
  val fruits = List("apple", "orange", "tomato", "banana", "kiwi")

  // println(nums.partition(x => x == 1))

  def myPack[T](xs: List[T]): List[List[T]] = xs match {
    case Nil => Nil
    case head :: tail =>
      val result: List[T] = head :: tail.takeWhile(x => x == head)
      println(result)
      result :: myPack(tail.dropWhile(x => x == head))
  }

  def pack[T](xs: List[T]): List[List[T]] = xs match {
    case Nil => Nil
    case head :: tail =>
      val (first, rest) = xs.span(x => x == head)
      first :: pack(rest)
  }

  println(myPack(List("a", "a", "a", "b", "c", "c", "a"))) // result: List(List("a", "a", "a"), List("b"), List("c", "c"), List("a"))
  println(pack(List("a", "a", "a", "b", "c", "c", "a"))) // result: List(List("a", "a", "a"), List("b"), List("c", "c"), List("a"))

  def encode[T](xs: List[T]): List[(T, Int)] =
    pack(xs).map(ys => (ys.head, ys.length))
}
