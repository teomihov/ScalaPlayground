package manning.functional.programming.part1

object Chapter4_HandlingErrorsWithoutExceptions extends App {
  // Book examples
  def failingFn(i: Int): Int = {
    try {
      val x = 42 + 5
      x + ((throw new Exception("fail!")): Int)
    } catch {
      case e: Exception => 43
    }
  }
  // println(failingFn(123))

  def mean(xs: Seq[Double]): Double = {
    if (xs.isEmpty)
      throw new ArithmeticException("mean of empty list!")
    else xs.sum / xs.length
  }

  trait Option[+A] {
    def map[B](f: A => B): Option[B]

    def flatMap[B](f: A => Option[B]): Option[B]

    def getOrElse[B >: A](default: => B): B

    def orElse[B >: A](ob: => Option[B]): Option[B]

    def filter(f: A => Boolean): Option[A]
  }

  case class Employee(name: String, department: String)

  def lookupByName(name: String): Option[Employee] =
    name match {
      case "Teo" => MySome(Employee("Teodor", "Engine"))
      case _ => MyNone
    }

  val joeDepartment: String =
    lookupByName("Teo").map(_.department).getOrElse("Няма")

  val teoFlatMapDep = lookupByName("Teo").flatMap(x => MySome(x.department)).getOrElse("Няма")
  println(joeDepartment)
  println(teoFlatMapDep)

  def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

  val absO: Option[Double] => Option[Double] = lift(math.abs)
  absO(MySome(5.55))

  def insuranceRateQuote(age: Int, numberOfSpeedingTickets: Int): Double = 55 // return some value - the idea is to skip the exception :D

  def parseInsuranceRateQuote(age: String, numberOfSpeedingTickets: String): Option[Double] = {
    val optAge = Try(age.toInt)
    val optTickets = Try(numberOfSpeedingTickets.toInt)

    map2(optAge, optTickets)(insuranceRateQuote)
  }

  def Try[A](a: => A): Option[A] =
    try MySome(a)
    catch {
      case e: Exception => MyNone
    }

  // Exercises

  // EXERCISE 4.1
  class MyOption[+A] extends Option[A] {
    override def map[B](f: A => B): Option[B] =
      this match {
        case MySome(v) => MySome(f(v))
        case MyNone => MyNone
      }

    override def flatMap[B](f: A => Option[B]): Option[B] =
      this match {
        case MySome(v) => f(v)
        case MyNone => MyNone
      }

    override def getOrElse[B >: A](default: => B): B =
      this match {
        case MySome(v) => v
        case MyNone => default
      }

    override def orElse[B >: A](ob: => Option[B]): Option[B] =
      this match {
        case MySome(v) => this
        case MyNone => ob
      }

    override def filter(f: A => Boolean): Option[A] =
      this match {
        case MySome(v) if f(v) => this
        case MyNone => MyNone
      }
  }

  case class MySome[A](get: A) extends MyOption[A]

  case object MyNone extends MyOption[Nothing]

  // EXERCISE 4.3
  def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
    (a, b) match {
      case (MySome(aValue), MySome(bValue)) => MySome(f(aValue, bValue))
      case _ => MyNone
    }
}
