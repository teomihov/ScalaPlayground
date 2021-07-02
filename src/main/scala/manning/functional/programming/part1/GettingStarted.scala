package manning.functional.programming.part1

import manning.functional.programming.part1.List.sum

object GettingStarted extends App {
  def abs(n: Int): Int =
    if (n < 0) -n
    else n

  // EXERCISE 2.1
  // Write a recursive function to get the nth Fibonacci number (http://mng.bz/C29s).
  // The first two Fibonacci numbers are 0 and 1 . The nth number is always the sum of the
  // previous two—the sequence begins 1, 1, 2, 3, 5 . Your definition should use a
  // local tail-recursive function.
  def fib(n: Int): Int = {
    def loop(n: Int, prev: Int, next: Int): Int = {
      if(n == 1) prev
      else loop(n - 1, next, prev + next)
    }

    loop(n, 1, 1)
  }

  println(s"Fib 4 = ${fib(4)}")
  println(s"Fib 11 = ${fib(11)}")

  // HOF
  def formatResult(name: String, n: Int, f: Int => Int) = {
    val msg = "The %s of %d is %d"
    msg.format(name, n, f(n))
  }

  println(formatResult("absolute value", -42, abs))
  println(formatResult("fibonacci", 10, fib))
  def findFirst[A](ss: Array[A], p: A => Boolean): Int = {
    @annotation.tailrec
    def loop(n: Int): Int = {
      if (n >= ss.length) -1
      else if (p(ss(n))) n
      else loop(n + 1)
    }

    loop(0)
  }

  println(findFirst[String](Array("a", "b", "c"), x => x == "b"))
  println(findFirst[String](Array("a", "b", "c"), x => x == "dd"))

  // EXERCISE 2.2
  // Implement isSorted , which checks whether an Array[A] is sorted according to a
  // given comparison function:
  def isSorted[A](as: Array[A], ordered: (A, A) => Boolean): Boolean = {
    def loop(n: Int): Boolean = {
      if (n >= as.length - 1) true
      else if (!ordered(as(n), as(n + 1))) false
      else loop(n + 1)
    }

    loop(0)
  }

  println(isSorted(Array(1, 2, 3, 4, 5), (x: Int, y: Int) => x < y))
  println(isSorted(Array(5, 4, 3, 2, 1), (x: Int, y: Int) => x > y))

  def partial1[A, B, C](a: A, f: (A, B) => C): B => C =
    (b: B) => f(a, b) // as same as b => f(a, b)

  // EXERCISE 2.3
  // Let’s look at another example, currying, 9 which converts a function f of two arguments
  // into a function of one argument that partially applies f . Here again there’s only one
  // implementation that compiles. Write this implementation.
  def curry[A, B, C](f: (A, B) => C): A => (B => C) =
    (a: A) => (b: B) => f(a, b)

  val curryFunction = curry((a: Int, b: Int) => a + b)
  curryFunction(2)(3)

  // EXERCISE 2.4
  // Implement uncurry , which reverses the transformation of curry . Note that since =>
  // associates to the right, A => (B => C) can be written as A => B => C .
  def uncurry[A, B, C](f: A => B => C): (A, B) => C =
    (a, b) => f(a)(b)

  val uncurryResult = uncurry(curryFunction)
  uncurryResult(2,3)

  // EXERCISE 2.5
  // Implement the higher-order function that composes two functions.
  def compose[A,B,C](f: B => C, g: A => B): A => C =
    (a: A) => f(g(a))

  def composeCheating[A,B,C](f: B => C, g: A => B): A => C =
    f.compose(g)

  // EXERCISE 3.1
  val x = List(1,2,3,4,5) match {
    case Cons(x, Cons(2, Cons(4, _))) => x
    case Nil => 42
    case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
    case Cons(h, t) => h + sum(t)
    case _ => 101
  }
  println(s"The result of exercise 3.1 is: $x")

}
