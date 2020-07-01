package lectures.oop

object Generics extends App{

  class MyList[+A]{
    def add[B >: A](element: B): MyList[B]= ???
    //use the type A
  }

  // generic methods
  object MyList{
    def empty[A]: MyList[A] = ???
  }

  //variance problem
  class Animal
  class Cat extends Animal
  class Dog extends Animal

  //1. YES List[Cat] extends List[Animal] = COVARIANCE
  class CovariantList[+A]
  val animal: Animal = new Cat
  val animalList: CovariantList[Animal] = new CovariantList[Cat]
  // animalList.add(new Dog) ??? HARD QUESTION => we return list of animals


  // 2. NO= INVARIANCE
  class InvariantList[A]
  val invariantAnimalList: InvariantList[Animal] = new InvariantList[Animal]

  // 3. Hell, no! CONTRAVARIANCE
  class Trainer[-A]
  val Trainer: Trainer[Cat] = new Trainer[Animal]

  //bounded class
  class Cage[A<:Animal] (animal: A)//A is subtype of Animal
   val cage = new Cage(new Dog)

  class Car
  val newCage = new Cage(new Car)


}
