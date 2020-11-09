package grokkingalgorithms

object QuickSort extends App  {
  def quickSort(arr: Seq[Int]): Seq[Any] = {
    if(arr.length < 2) arr
    else {
      val pivot = arr.head
      val less = arr.tail.filter(_ < pivot)
      val bigger= arr.tail.filter(_ > pivot)
      //      val lessQS = quickSort(less)
      //      val lessPlusPivot = lessQS :+ pivot
      //      val biggerQs = quickSort(bigger)
      //      lessPlusPivot ++ biggerQs
      (quickSort(less) :+ pivot) ++ quickSort(bigger)
    }
  }

  val quickSortResult = quickSort(Seq(1,7,3,2,5))
  println(quickSortResult)
}
