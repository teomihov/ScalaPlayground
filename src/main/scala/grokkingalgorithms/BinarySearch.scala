package grokkingalgorithms

import scala.language.postfixOps

object BinarySearch extends App  {
  val array = 1 to 1000 toArray
  def binarySearch(target: Int, numbers: Array[Int]): Boolean = {
    if(target < numbers(0) || target > numbers(numbers.length -1)) return false

    val currentNumber = numbers(numbers.length / 2)
    if(target == currentNumber) true
    else if(target > currentNumber) binarySearch(target, numbers.filter(_ > currentNumber))
    else binarySearch(target, numbers.filter(_ < currentNumber))
  }

  println(binarySearch(22, array))
  println(binarySearch(222, array))
  println(binarySearch(2222, array))

  trait Functor[F[_]] {
    def fmap[A,B](f:A=>B, fa: F[A]):F[B]
    //(<$) in Haskell
    def left[C,D](a:C, fb:F[D]):F[C] = {
      val test: Any => C = Function.const(a)
      fmap(Function.const(a), fb)
    }
  }

  val a = Seq(12,3,4,5)
  println(a)


  var d = Map.empty[Int, String]
  d = d + (1 -> "test")
  d = d + (2 -> "teo")
  d = d + (2 -> "asd")
  d = d + (1 -> "test1")

  val test = Some(1)
  val rr = d.filter(x => test.getOrElse(5).equals(x._1))

  println(rr)
  println(d)
}
