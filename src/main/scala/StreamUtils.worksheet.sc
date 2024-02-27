import cats.effect.SyncIO
import cats.effect.kernel.Resource
import cats.effect.IO
import cats.effect.Async
import fs2.{Pipe, Pull,Stream}

import scala.concurrent.duration.FiniteDuration

import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._
object StreamUtils {

  case class TimeoutException(duration: FiniteDuration) extends RuntimeException(s"No new messages received for $duration")

  def timeoutOnIdle[F[_] : Async, A](duration: FiniteDuration): Pipe[F, A, A] = { stream =>
    stream
      .pull
      .timed { timedPull =>
        def go(timedPull: Pull.Timed[F, A]): Pull[F, A, Unit] =
          timedPull.timeout(duration) >>
            timedPull.uncons.flatMap {
              case Some((Right(elems), next)) => Pull.output(elems) >> go(next)
              case Some((Left(_), _)) => Pull.raiseError(TimeoutException(duration))
              case None => Pull.done
            }

        go(timedPull)
      }
      .stream
  }



//implicit val ec = IORuntime.global
  val s = (Stream("elem") ++ Stream.sleep_[IO](1500.millis)).repeat.take(3)

  s.pull
          .timed { timedPull =>
            def go(timedPull: Pull.Timed[IO, String]): Pull[IO, String, Unit] =
             timedPull.timeout(1.second) >> // starts new timeout and stops the previous one
               timedPull.uncons.flatMap {
                 case Some((Right(elems), next)) => Pull.output(elems) >> go(next)
                 case Some((Left(_), next)) => Pull.output1("late!") >> go(next)
               case None => Pull.done
               }
             go(timedPull)
          }.stream.compile.toVector.unsafeRunSync()
}



// In fs2, the scan operation is used to perform a stateful streaming transformation on an fs2.Stream. It is similar to the scanLeft operation in Scala collections, but it operates on a stream of elements instead of a static collection.

// The scan operation in fs2 takes an initial state and a binary function that updates the state based on each incoming element of the stream. It produces a new stream of elements that represent the intermediate states as the stream is processed.

// Here's a basic example of how to use scan in fs2:




val stream= Stream(1, 2, 3, 4, 5)

val scanStream = stream.scan(1)((acc, elem) => acc + elem)

scanStream.toList

