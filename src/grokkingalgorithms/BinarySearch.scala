package grokkingalgorithms
// test ammend
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
}
