# FS2-Examples

In general you want to ask for the least powerful kind of function; `A=>B, A=>F[B], A=>Stream[F, B]`, etc 
for Kafka, `Chunk[A] => F[Chunk[B]] or Chunk[A] => F[Unit]` are optimal 

The right association ensures that the operations are composed in a way that is efficient and stack-safe.

Recursive Operations and Stack Safety:
Recursive operations, especially when dealing with monads, have the potential to cause stack overflow errors if not implemented carefully.

Each recursive call consumes space on the call stack, and if the recursion goes too deep, it can lead to a stack overflow.
Tail Recursion and Right Association:
Right association is crucial for achieving tail recursion, which is a technique that allows certain recursive functions to be optimized by the Scala compiler.
Tail-recursive functions are designed so that the recursive call is the last operation in the function, making it compatible with optimizations like tail call optimization (TCO).
Trampolining for Stack Safety:
In scenarios where direct tail recursion is not possible, trampolining can be employed to achieve stack safety. Trampolining involves using a loop to repeatedly apply functions until the final result is reached.
Right association facilitates trampolining by ensuring that the recursive calls are performed in a way that avoids stack buildup.

rampolining is a technique used in functional programming to achieve stack safety when dealing with potentially deep recursion. In Scala, this is particularly relevant when working with recursive monadic operations or other recursive functions that might otherwise lead to a stack overflow.


Free Monads and Trampolining:
Trampolining is often used in conjunction with the concept of Free Monads. Free Monads provide a way to build complex, domain-specific languages by representing computations as a data structure. Trampolining can be applied to make the interpretation of Free Monad structures stack-safe.

Trampolining is closely related to Continuation Passing Style (CPS), where functions take an additional argument representing the continuation of the computation. While not always the same, trampolining often involves transforming a recursive function into CPS to achieve stack safety.

Unordered means that we don't care about the order of results from the evalMap action.


It allows for higher throughput because it never waits to start a new job. It has N job permits (with a semaphore), and the moment one job finishes, it requests the next element from the stream and begins operation.


Contramap is essentially the opposite of map. While map operates on the "output" or "result" of a computation, contramap works on the "input" or "parameter" of a computation

```scala
trait Printable[A] {
  def format(value: A): String

  def contramap[B](f: B => A): Printable[B] =
    (value: B) => format(f(value))
}

```
[system-from-scratch-in-scala-3](https://chollinger.com/blog/2023/06/building-a-functional-effectful-distributed-system-from-scratch-in-scala-3-just-to-avoid-leetcode-part-1/)
[fold](https://www.baeldung.com/scala/folding-lists)

[pull](https://kebab-ca.se/chapters/fs2/overview.html)

http://www.gibbons.org.uk/scala3-fs2-july-2021

https://github.com/PendaRed/scala3-fs2/tree/main/src/main/scala/com/jgibbons/fs2/c





