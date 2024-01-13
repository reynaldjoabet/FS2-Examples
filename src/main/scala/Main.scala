import fs2._
import cats.effect._

object Main extends App {

  val fibonacciStream = Stream.unfold(0)(x => Some(x + (x + 1), x + 1))
  println("Hello, FS2 Streams!")
}
