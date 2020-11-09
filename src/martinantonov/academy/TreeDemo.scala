package martinantonov.academy

class Tree[T](value: T, children: Tree[T]*) {
  private var root: Vertex[T] = new Vertex[T](value)

  if(children != null && !children.isEmpty) children.foreach(x => root.addChild(x.root))
}

class Vertex[T](var value: T) {
  private var children: List[Vertex[T]] = List.empty[Vertex[T]]
  private var hasParent: Boolean = false

  def childrenCount: Int = this.children.count(x => true)

  def addChild(child: Vertex[T]) = {
    if(child == null) throw new Exception("Child cannot be null")
    else if(child.hasParent) throw new Exception("The vertex already has a parent")
    else {
      child.hasParent = true
      this.children = children :+ child
    }
  }

  def getChild(index: Int):Vertex[T] = {
    if(index >= this.children.length || index < 0) throw new Exception("Out of range")
    else this.children(index)
  }
}

object TreeTest extends App {
  val tree = new Tree[Int](1,
    new Tree[Int](2,
      new Tree[Int](5),
      new Tree[Int](6)),
    new Tree[Int](3),
    new Tree[Int](4,
      new Tree[Int](7),
      new Tree[Int](8),
      new Tree[Int](9)))

  println(tree)
}
