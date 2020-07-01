package exercises

object OOBasics extends App {


  val author = new Writer("Charls","Dickens", 1812)
  val imposter = new Writer("Charls","Dickens", 1812)
  val novel = new Novel("Great Expectations", 1861, author)

  println(novel.authorAge())
  println(novel.isWrittenBy(imposter))


  val counter = new Counter

  counter.inc.print
  counter.inc.inc.inc.print
  counter.inc(10).print

  /*
  *   Novel and Writer
  *
  * Writer: firstname, surname, year
  * - method fullname
  *
  * Novel: name? year of release, author
  * - author age
  *  - isWrittenBy(author)
  * - copy(new year of release) = new instance of Novel
  *
  * */
  class Writer(firstName: String, surname: String, val year: Int) {
    def fullName(): String = {
      firstName + " " + surname
    }
  }

  class Novel(name: String, yearOfRelease: Int, author: Writer) {
    def authorAge(): Int = {
      yearOfRelease - author.year
    }

    def isWrittenBy(author: Writer): Boolean = author == this.author

    def copy(newYearOfRelease: Int): Novel = {
      new Novel(name, newYearOfRelease, author)
    }
  }

  /*
  * Counter class
  *  - receive an int value
  * - method current count
  * - method to increment/decrement => new Counter
  * -overload inc/dec to receive an amount
  *
  * */
  class Counter( val count: Int=0) {

    def inc = {
      println("incrementing")
      new Counter(count + 1)
    }
    def dec = {
      println("decrementing")
      new Counter(count - 1)
    }

    def inc(n: Int):Counter=
      if(n<=0) this
      else inc.inc(n-1)


    def dec(n: Int):Counter=
      if(n<=0) this
      else dec.dec(n-1)

def print = println(count)

  }

}
