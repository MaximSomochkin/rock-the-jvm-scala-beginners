package playground

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}

object Playground extends App{

   implicit val actorSystem = ActorSystem("Playground")
    //val materializer = Materializer //depricated
  Source.single("hello, Streams!").to(Sink.foreach(println)).run()

}
