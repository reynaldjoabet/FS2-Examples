import fs2._
import cats.effect._

object Main extends IOApp {

  val fibonacciStream = Stream.unfold(0)(x => Some(x + (x + 1), x + 1))

  val p = (x: Int) => IO.println(s"this is an Int ${x}")

  val q = (x: Int) => IO.println(s"this is a String ${x.toString()}")

  override def run(args: List[String]): IO[ExitCode] =
    Stream(1, 2)
      .evalTap(p)
      .evalMap(q)
      .compile.drain.as(ExitCode.Success)

}
