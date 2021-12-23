package manning.functional.programming.part1

import scala.{:+, ::}

object Chapter3_FunctionalDataStructures extends App {
  val l = List(1, 2, 3, 4, 5, 6)
  // println(removeFirstElement(l))

  // EXERCISE 3.3
  // println(setHead(l, 22))

  // EXERCISE 3.4
  def drop[A](l: List[A], n: Int): List[A] = (l, n) match {
    case (Nil, _) => l
    case (_, 0) => l
    case (Cons(h, t), _) =>
      drop(t, n - 1)
  }

  // println(drop(l, 10))


  // EXERCISE 3.5
  def dropWhile[A](l: List[A], f: A => Boolean): List[A] = l match {
    case Cons(h, t) if f(h) => dropWhile(t, f)
    case _ => l
  }

  // println(dropWhile(l, (x: Int) => x == 111))

  // Exercise 3.6
  def init[A](l: List[A]): List[A] = {
    def loop(l: List[A], acc: List[A]): List[A] = l match {
      case Nil => acc
      case Cons(_, Nil) => acc
      case Cons(h, t) => Cons(h, loop(t, acc))
    }

    val r = loop(l, List())
    r
  }

  // println(init(l))

  def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B =
    as match {
      case Nil => z
      case Cons(x, xs) => f(x, foldRight(xs, z)(f))
    }

  def sum2(ns: List[Int]) =
    foldRight(ns, 0)((x, y) => x + y)

  def product2(ns: List[Double]) =
    foldRight(ns, 1.0)(_ * _)

  // EXERCISE 3.8
  println(foldRight(List(1, 2, 3, 4, 5), Nil: List[Int])(Cons(_, _)))

  // EXERCISE 3.9
  def length[A](as: List[A]): Int =
    foldRight(as, 0)((_, y) => y + 1)

  //println(length(List(1,2,3,4,5,6)))

  // EXERCISE 3.10
  def foldLeft[A, B](as: List[A], z: B)(f: (B, A) => B): B = {
    def loop(elements: List[A], acc: B): B = {
      elements match {
        case Nil => acc
        case Cons(head, tail) => loop(tail, f(acc, head))
      }
    }

    loop(as, z)
  }

  /*
    loop([1,2,3,4,5], 0)
    loop([2,3,4,5], f([1, Nil]))
    loop([3,4,5], f(Cons(2, Cons(1, Nil))
    loop([4,5], f(Cons(3, Cons(2, Cons(1, Nil)))
    loop([5], f(Cons(4,Cons(3, Cons(2, Cons(1, Nil))))
    loop([], f(Cons(5,Cons(4,Cons(3, Cons(2, Cons(1, Nil))))
   */
  println(foldLeft(List(1, 2, 3, 4, 5), Nil: List[Int])((x, y) => Cons(y, x)))

  // EXERCISE 3.11
  def sumWithFoldLeft(as: List[Int]) =
    foldLeft(as, 0)(_ + _)

  def productWithFoldLeft(as: List[Int]) =
    foldLeft(as, 1)(_ * _)

  // EXERCISE 3.12
  def reverse(as: List[Int]) = {
    val r = ""
    foldLeft(as, r)((x, y) => y + "" + x) // TODO: see how to use list :+ element because in the moment it doesn't work
  }

  println(reverse(List(5, 4, 3, 2, 1, 1, 2, 3, 4, 5)))

  // EXERCISE 3.13
  // TODO: this solution not working as expected
  def foldRightFromFoldLeft[A, B](as: List[A], z: B)(f: (A, B) => B): B = {
    foldLeft(as, z)((x, y) => f(y, x))
  }

  //println(foldRightFromFoldLeft(List(1,2,3,4,5), Nil:List[Int])((x, y) => Cons(x,y)))

  // EXERCISE 3.14
  // def append[A](xs: List[A], ys: List[A]): List[A] = foldRight(xs, ys)(_ :: _) // operator :: doesn't work. As same as :+

  sealed trait Tree[A]

  case class Leaf[A](value: A) extends Tree[A]

  case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]


  // EXERCISE 3.25
  val aTree = Branch(Branch(Leaf(1), Leaf(5)), Branch(Leaf(3), Leaf(4)))

  def size(tree: Tree[Int]): Int = {
    tree match {
      case Leaf(_) => 1
      case Branch(l, r) => size(l) + size(r) + 1
    }
  }

  // println(size(aTree))

  // EXERCISE 3.26
  def findMax(tree: Tree[Int]) = {
    def loop(insideTree: Tree[Int], acc: Int): Int = {
      insideTree match {
        case l@Leaf(v) =>
          if (v > acc) loop(l, v)
          else acc
        case Branch(l, r) =>
          val leftMax = loop(l, acc)
          loop(r, leftMax)
      }
    }

    loop(tree, -100000)
  }

  println(findMax(aTree))



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
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  def append[A](a1: List[A], a2: List[A]): List[A] =
    a1 match {
      case Nil => a2
      case Cons(h, t) => Cons(h, append(t, a2))
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