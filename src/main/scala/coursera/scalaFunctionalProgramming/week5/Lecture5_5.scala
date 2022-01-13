package coursera.scalaFunctionalProgramming.week5

object Lecture5_5 extends App {
  def sum(xs: List[Int]) = (0 :: xs) reduceLeft (_ + _)
  def product(xs: List[Int]) = (1 :: xs) reduceLeft (_ * _)

  def sum1(xs: List[Int]) = xs.foldLeft(0)(_ + _)
  def product1(xs: List[Int]) = xs.foldLeft(1)(_ * _)

  def concat[T](xs: List[T], ys: List[T]): List[T] =
    (xs foldLeft  ys) (_ :+ _)

  val first = List(1,2,3,4,5)
  val second = List(6,7,8,9)

  // println(concat(first, second))


  case class Person(name: String, age: Int)
  val users: Map[Int, Person] = Map(1 -> Person("Tom", 10), 2 -> Person("Gillian", 13), 3 -> Person("Sarah", 11), 4 -> Person("David", 20))
  val peopleList: List[Person] = users.foldLeft(List.empty[Person])((people, current) => {
    if (current._2.age > 10) people :+ current._2
    else people
  })

  println(peopleList)

  val youngestPerson: Person = peopleList.reduceLeft((youngestPerson, currentPerson) => {
    if (youngestPerson.age > currentPerson.age) currentPerson
    else youngestPerson
  })

  println(youngestPerson)

  val youngestPersonWithFold = peopleList.foldLeft(Person("lastone", 1000))((youngestPerson, currentPerson) => {
    if (youngestPerson.age > currentPerson.age) currentPerson
    else youngestPerson
  })

  println(youngestPersonWithFold)
}
