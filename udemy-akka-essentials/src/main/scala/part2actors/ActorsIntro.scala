package part2actors

import akka.actor
import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // part1- actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  //part2 - create actors
  // word count actor

  class WordCountActor extends Actor {
    //internal dada
    var totalWords = 0
    //behavior
    override def receive: Receive = { // override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word counter] I have receved: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot uderstand ${msg.toString}")
    }
  }
// part3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  //parrt4 - communicate!
  wordCounter ! "I am learning Akka and It's pretty cool!" //tell
  wordCounter ! "A different message"
  //asynchronous

  //pass arguments

  object Person{
    def props(name: String): Props = Props(new Person(name)) //Best practice
  }

  class Person(name: String) extends Actor{
    override def receive: Receive = {
      case  "hi" => println(s"Hi, my name is $name")
      case  _ =>
    }
  }

  //val person = actorSystem.actorOf(Props(new Person("Bob")))
  val person = actorSystem.actorOf(Person.props("Bob")) //best practice
  person ! "hi"
}
