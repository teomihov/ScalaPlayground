package grokkingalgorithms

object SelectionSort extends App {
  def selectionSort(numbers: Array[Int]): Array[Int] = {
    def findMinNumber(currentMin: Int, numbers: List[Int]): Int = {
      if(numbers.isEmpty) currentMin
      else if(currentMin > numbers.head) findMinNumber(numbers.head, numbers.tail)
      else findMinNumber(currentMin, numbers.tail)
    }

    def selectionSortWithAggregator(numbers: Array[Int], aggregator: Array[Int]): Array[Int] =
      if(numbers.isEmpty) aggregator
      else {
        val minNumber = findMinNumber(numbers(0), numbers.toList)
        selectionSortWithAggregator(numbers.filter(_ != minNumber), aggregator :+ minNumber)
      }

    selectionSortWithAggregator(numbers, Array())
  }

  val result = selectionSort(Array(1,7,3,2,5))
  println(result.toList)
}
