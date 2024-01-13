import fs2._
import cats.effect._
import scala.concurrent.duration._
import fs2.concurrent.Topic

object TopicExample extends IOApp {

  val stream2 =
    fs2.Stream
      .unfold(1)(x => Some(x, x + 1))
      .chunks
      .mapAccumulate(1)((l, c) => (c.size + l, c))
      .evalMap(IO.println)
      .as(2)
      .chunks
      .covary[IO]

  val consumer2: Pipe[IO, Chunk[Int], Unit] =
    _.evalMap(i => IO.println(s"Read $i from consumer2"))

  val consumer1: Pipe[IO, Chunk[Int], Unit] =
    _.evalMap(i => IO.println(s"Read $i from consumer1"))

  val consumer3: Pipe[IO, Chunk[Int], Unit] =
    _.evalMap(i => IO.println(s"Read $i from consumer3"))

  stream2.pull.uncons

  // Stream.resource()

  override def run(args: List[String]): IO[ExitCode] = stream2
    // .metered(2.seconds)
    // .buffer(3)
    .broadcastThrough(consumer1, consumer2, consumer3)
    .compile
    .drain
    .as(ExitCode.Success)

}
