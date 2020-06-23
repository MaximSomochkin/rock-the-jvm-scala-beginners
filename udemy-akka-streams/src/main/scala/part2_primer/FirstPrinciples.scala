package part2_primer

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {

  implicit val system: ActorSystem = ActorSystem("FirstPrinciples")

  //sources
  val source = Source(1 to 10)
  //sinks
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)
  //graph.run()

  //flows transforms elements

  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

  //  sourceWithFlow.to(sink).run()
  //  source.to(flowWithSink).run()
  //  source.via(flow).to(sink).run()

  /** nulls are NOT allowed */
  //  val illigalSource = Source.single[String](null)
  //  illigalSource.to(Sink.foreach(println)).run()
  // use Options instead
  val illigalSourceOption = Source.single[Option[String]](Option(null))
  illigalSourceOption.to(Sink.foreach(println)).run()

  // various kinds of sources
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1, 2, 3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(LazyList.from(1))

  import scala.concurrent.ExecutionContext.Implicits.global

  val futureSource = Source.future(Future(42))

  // sinks
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] // retrives head and then closes the stream
  val foldSink = Sink.fold[Int, Int](0)((a, b) => a + b)

  // flows - usually mapped to collection operators
  val mapFlow = Flow[Int].map(x => 2 * x)
  val takeFlow = Flow[Int].take(5)
  // drop, filter
  //NOT have flatmap, because cannot have substreams

  //source -> flow -> flow ->... -> sink
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)
  doubleFlowGraph.run()

  //syntatic sugars
  val mapSource = Source(1 to 10).map(x => x * 2) //Source(1 to 10).via(Flow[Int].map(x => x*2))
  //run streams directly
  mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println)).run

  /**
   * Exercise: create a stream that takes the names of person,
   * then you will keep the first 2 names with length > 5 characters
   * */
  val names = List("John","Maximus","Bob", "RobbyWilliams", "Sarah")

  val nameSource = Source(names)
  val longNameFlow = Flow[String].filter(name => name.length > 5)
  val limitFlow = Flow[String].take(2)
  val nameSink = Sink.foreach[String](println)
  nameSource.via(longNameFlow).via(limitFlow).to(nameSink).run()

  //OR
  Source(names).filter(_.length > 5).take(2).runForeach(println)




}
