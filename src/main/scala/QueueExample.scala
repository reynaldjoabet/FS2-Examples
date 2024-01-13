import fs2._
import cats.effect.std._
import cats.effect._
import scala.concurrent.duration.DurationInt
import cats.implicits._
object QueueExample {

  class Buffering[F[_]: Concurrent: Console](
      q1: Queue[F, Int],
      q2: Queue[F, Int]
  ) {
    def start: Stream[F, Unit] = Stream(
      Stream.range(0, 1000).covary[F].foreach(q1.offer),
      Stream.repeatEval(q1.take).foreach(q2.offer),
      // .map won't work here as you're trying to map a pure value with a side effect
      Stream
        .repeatEval(q2.take)
        .foreach(n => Console[F].println(s"Pulling out $n from Queue #2"))
    ).parJoin(3)
  }
  val streamz =
    for {
      q1 <- Stream.eval(Queue.bounded[IO, Int](1))
      q2 <- Stream.eval(Queue.bounded[IO, Int](100))
      bp = new Buffering[IO](q1, q2)
      _ <- Stream.sleep[IO](5.seconds).concurrently(bp.start.drain)
    } yield ()

  def queues(queue: Queue[IO, Int]) =
    Stream.fromQueueUnterminated(queue)

  def queues1(queue: Queue[IO, Int]) =
    Stream.emits(1 to 1000).enqueueUnterminated(queue)

  def queues2(queue: Queue[IO, Chunk[Int]]) =
    Stream.emits(1 to 1000).enqueueUnterminatedChunks(queue)
  val queue = for {
    queue <- Queue.unbounded[IO, Option[Int]]
    streamFromQueue = Stream.fromQueueNoneTerminated(queue)
    _ <- Seq(Some(1), Some(2), Some(3), None).map(queue.offer).sequence
    result <- streamFromQueue.compile.toList
  } yield result
}
