package manning.functional.programming.part1

import manning.functional.programming.part1.List.{removeFirstElement, setHead}

object FunctionalDataStructures extends App {
  val l = List(1,2,3,4,5,6)
  println(removeFirstElement(l))

  // EXERCISE 3.3
  println(setHead(l, 22))

  // EXERCISE 3.4
  def drop[A](l: List[A], n: Int): List[A] = (l, n) match {
    case (Nil, _) => l
    case (_, 0) => l
    case (Cons(h, t), _) =>
      drop(t, n-1)
  }
  println(drop(l, 10))


  // EXERCISE 3.5
  def dropWhile[A](l: List[A], f: A => Boolean): List[A] = l match {
    case Nil => l
    case Cons(h, _) if f(h) => l
    case Cons(h, t) if !f(h)  => dropWhile(t, f)
  }

  println(dropWhile(l, (x:Int) => x == 111))
}

sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List {
  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Cons(x, xs) => x + sum(xs)
  }

  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x, xs) => x * product(xs)
  }
  
  def apply[A](as: A*): List[A] =
    if(as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  def append[A](a1: List[A], a2: List[A]): List[A] =
    a1 match {
      case Nil => a2
      case Cons(h,t) => Cons(h, append(t, a2))
    }

  // EXERCISE 3.2
  def removeFirstElement[A](ds: List[A]): List[A] = ds match {
    case Nil => ds
    case Cons(h, t) => t
  }

  // EXERCISE 3.3
  def setHead[A](ds: List[A], hValue: A): List[A] = ds match {
    case Nil => ds
    case Cons(h, t) => Cons(hValue, t)
  }
}