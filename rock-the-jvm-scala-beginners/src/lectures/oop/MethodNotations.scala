package lectures.oop

object MethodNotations extends App{

  class Person(val name: String, favoritMovie: String){
    def likes(movie: String): Boolean = movie==favoritMovie
    def + (person: Person): String = s"${this.name} is hanging out with ${person.name}"
    def unary_! : String = s" $name? what the heck!?"
    def isAlive: Boolean = true

    def apply(): String = s"Hi my name is $name and I like $favoritMovie "
  }

  // infix notation  = operator notation (syntactic sugar)
  // if method has single parameter it can be called in this INFIX notation style
  val mary = new Person("Mary", "Inception")
  println(mary.likes("Inception"))
  println(mary likes "Inception")


  //Operators in Scala
  // ALL OPERATORS ARE METHODS
  val tom = new Person("Tom","Flight club")
  println(mary + tom)
  println(mary.+(tom))


  //prefix notations
  // unary_prefix ONLY WORKS WITH - + ~ !
  val x = -1 //equivalent with 1.unary_-
  val y = 1.unary_-

  println(!mary)//equivalent with mary.unary_!
  println(mary.unary_!)

  //postfix notation
  println(mary.isAlive) //more often use in practice
  println(mary isAlive)

  // apply
  println(mary.apply())
  println(mary()) //equivalent
}
