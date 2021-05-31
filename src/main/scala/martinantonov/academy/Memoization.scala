package martinantonov.academy

object Memoization extends App {

  def fibonacci(n: Int): Int = {
    val memo =  Array.ofDim[Int](16)

    def fibonacciHelper(n: Int): Int = {
      if(memo(n) != 0) return memo(n)
      else if(n < 2) return 1
      else {
        memo(n) = fibonacciHelper(n - 1) + fibonacciHelper(n - 2)
        memo(n)
      }
    }

    fibonacciHelper(n)
  }

  def normalFibonacci(n: Int): Int = {
    if(n < 2) 1
    else normalFibonacci(n-1) + normalFibonacci(n - 2)
  }

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }
  println("Memoization: ")
  time { fibonacci(15) }

  println("Normal: ")
  time { normalFibonacci(15) }
}
