package martinantonov.academy

import scala.annotation.tailrec

class LinkedListNode[T](val value:T) {
  private var _previous: LinkedListNode[T] = null
  private var _next: LinkedListNode[T] = null

  def previous = _previous
  def previous_= (newValue: LinkedListNode[T]):Unit = _previous = newValue

  def next = _next
  def next_= (newValue: LinkedListNode[T]):Unit = _next = newValue
}

class LinkedList[T] {
  var _count: Int = 0
  var first: LinkedListNode[T] = null
  var last: LinkedListNode[T] = null

  def addLast(value: T): LinkedListNode[T] = {
    var newNode = new LinkedListNode[T](value)

    if(this.first == null) {
      this.first = newNode
      this.last = newNode
    }

    this.last.next = newNode
    newNode.previous = this.last
    this.last = newNode // equals to newNode.next = null

    _count += 1

    newNode
  }

  def addFirst(value: T): LinkedListNode[T] = {
    var newNode = new LinkedListNode[T](value)
    if(this.first == null) {
      this.first = newNode
      this.last = newNode
    }

    this.first.previous = newNode
    newNode.next = this.first
    this.first = newNode

    _count += 1

    newNode
  }

  def addBefore(node: LinkedListNode[T], value: T): LinkedListNode[T]  = {
    if(node == null) throw new Exception("Node cannot be null")
    if(node.previous == null) addFirst(value)
    else {
      val newNode =  new LinkedListNode[T](value)
      newNode.next = node
      newNode.previous = node.previous

      node.previous.next = newNode
      node.previous = newNode

      _count += 1

      newNode
    }
  }

  def addAfter(node: LinkedListNode[T], value: T): LinkedListNode[T] = {
    if (node == null || value == null) throw new Exception("Neither value or node cannot be null")
    val newNode = new LinkedListNode[T](value)
    newNode.previous = node
    // Update the 'after' link's next reference, so its previous points to the new one
    if (node.next != null)
      node.next.previous = newNode
    // Steal the next link of the node, and set the after so it links to our new one
    newNode.next = node.next
    node.next = newNode

    _count += 1

    newNode
  }

  def count(): Int = _count

  def remove(node: LinkedListNode[T]): Unit = {
    if(node == null) throw new Exception("Missing node")
    else {
      if(node.previous == null) {
        this.first = this.first.next
        if(this.first != null) this.first.previous = null
      }
      else if(node.next == null) {
        this.last = this.last.previous
        if(this.last != null) this.last.next = null
      }
      else {
        node.previous.next = node.next
        node.next.previous = node.previous
      }

      _count -= 1
    }
  }

  def remove(value: T): Unit = {
    @tailrec
    def findNode(value: T, node: LinkedListNode[T]): LinkedListNode[T] = {
      if(node.next == null) null
      if(node.value == value) node
      else findNode(value, node.next)
    }

    remove(findNode(value, this.first))
  }
}

object Test extends App {
  var list = new LinkedList[Int]()
  list.addLast(1)
  val thirdNode = list.addLast(3)
  list.addLast(5)
  list.addFirst(0)
  list.addBefore(thirdNode, 2)
  list.addAfter(thirdNode, 4)

  list.remove(2)
  list.remove(0)
  list.remove(5)

  printElement(list.first)
  println("Count: " + list.count)

  @tailrec
  def printElement(currentNode: LinkedListNode[Int]):Any = {
    if(currentNode == null) println
    else {
      print(currentNode.value + " ")
      printElement(currentNode.next)
    }
  }
}