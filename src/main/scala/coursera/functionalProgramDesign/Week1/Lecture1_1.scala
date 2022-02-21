package coursera.functionalProgramDesign.Week1

object Lecture1_1 extends App {
  case class Book(title: String, authors: List[String])
  val books = List(
    Book("Structure and ...", List("Abelson", "Sussman", "Gerald")),
    Book("Introduction to ...", List("Bird", "Wadler")),
    Book("Effective Java", List("Bloch")),
    Book("Programming in Scala", List("Odersky", "Spoon", "Vanners")),
    Book("Java Puzzlers", List("Bloch", "Gafter")),
  )

  // find books whose author's name is "Bird"
  val birdsBooks = for {
    b <- books if(b.authors.contains("Bird"))
  } yield b.title

  println(birdsBooks)

  // find the names of all authors who have written at least two books
  val authorsWith2Books = for {
    b1: Book <- books.toSet
    b2 <- books if (b1 != b2)
    a1 <- b1.authors
    a2 <- b2.authors if (a1 == a2)
  } yield a2

  println(authorsWith2Books)
}
