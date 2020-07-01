package part2_primer

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}

object MaterializingStreams  extends App{

  implicit val system = ActorSystem("MaterializingStreams")
  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
  val simpleMaterializedValue = simpleGraph.run()


}
