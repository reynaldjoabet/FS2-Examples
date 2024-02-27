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

## Pull 
  The output values of a pull are emitted not one by one, but in chunks.
  A `Chunk` is an immutable sequence with constant-time indexed lookup. For example,a pull `p: Pull[F, Byte, R]` internally operates and emits `Chunk[Byte]` values, which can wrap unboxed byte arrays -- avoiding boxing/unboxing costs.The `Pull` API provides mechanisms for working at both the chunk level and the individual element level. Generally, working at the chunk level will result in better performance but at the cost of more complex implementations

  A pull only emits non-empty chunks.

  However, chunks are not merely an operational matter of efficiency. Each pull is emitted from a chunk atomically, which is to say, any errors or interruptions in a pull can only happen between chunks, not within a chunk. For instance, if creating a new chunk of values fails (raises an uncaught exception) while creating an intermediate value, then it fails to create the entire chunk and previous values are discarded.

```scala
Pull[F[_],+O,+R]
```
- Reads values from one or more streams
- returns a result of type R
- and produces a `Stream[F,O]` when calling `stream` method



In Haskell `uncons` is used to Decompose a list into its head and tail.
If the list is empty, returns Nothing.
If the list is non-empty, returns Just (x, xs), where x is the head of the list and xs its tail.
 ```scala
 uncons []
 //Nothing

 uncons [1]
 //Just (1,[])
 uncons [1, 2, 3]
 //Just (1,[2,3])

 ```

the library implements the Stream type functions using the Pull type 

The `Pull[F[_], O, R]` type represents a program that can pull output values of type `O` while computing a result of type `R` while using an effect of type `F`

The result `R` represents the information available after the emission of the element of type `O` that should be used to emit the next value of a stream. For this reason, using `Pull` directly means to develop recursive programs

The `Pull` type represents a stream as a head and a tail, much like we can describe a list. The element of type `O` emitted by the `Pull` represents the head. However, since a stream is a possible infinite data structure, we cannot express it with a finite one. So, we return a type `R`, which is all the information that we need to compute the tail of the stream
We can convert a `Pull` having the `R` type variable bound to Unit directly to a `Stream` by using the `stream` method

 A `Pull` that returns Unit is like a List with a head and empty tail.
 Unlike the `Stream` type, which defines a monad instance on the type variable `O`, a `Pull` forms a monad instance on `R`. If we think, it’s logical: All we want is to concatenate the information that allows us to compute the tail of the stream.

 we can convert a `Stream` into a `Pull` using the `echo` method:
```scala
Stream(7).pull.echo// convert a Stream to a Pull
```
 Another way to convert a `Stream` into a `Pull` is to use the `uncons` function, which returns a `Pull` pulling a tuple containing the head chunk of the stream and its tail



```scala
 implicit final class StreamPullOps[F[_], O](private val self: Pull[F, O, Unit]) extends AnyVal {
private[fs2] def uncons: Pull[F, Nothing, Option[(Chunk[O], Pull[F, O, Unit])]] =
      self match {
        case Succeeded(_)    => Succeeded(None)
        case Output(vals)    => Succeeded(Some(vals -> unit))
        case ff: Fail        => ff
        case it: Interrupted => it
        case _               => Uncons(self)
      }

  }

final class ToPull[F[_], O] private[Stream] (
      private val self: Stream[F, O]
  ) extends AnyVal {

 def uncons: Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]] =
      self.underlying.uncons.map(_.map { case (hd, tl) => (hd, tl.streamNoScope) })

def echo1: Pull[F, O, Option[Stream[F, O]]] =
      uncons.flatMap {
        case None => Pull.pure(None)
        case Some((hd, tl)) =>
          val (pre, post) = hd.splitAt(1)
          Pull.output(pre).as(Some(tl.cons(post)))
      }

 /** Reads the next available chunk from the input and emits it to the output. */
  def echoChunk: Pull[F, O, Option[Stream[F, O]]] =
      uncons.flatMap {
        case None           => Pull.pure(None)
        case Some((hd, tl)) => Pull.output(hd).as(Some(tl))
      }
  } 

  implicit final class StreamPullOps[F[_], O](private val self: Pull[F, O, Unit]) extends AnyVal {

  /** `Pull` transformation that takes the given stream (pull), unrolls it until it either:
    * - Reaches the end of the stream, and returns None; or
     * - Reaches an Output action, and emits Some pair with
     *   the non-empty chunk of values and the rest of the stream.
     */
  private[fs2] def uncons: Pull[F, Nothing, Option[(Chunk[O], Pull[F, O, Unit])]] =
      self match {
        case Succeeded(_)    => Succeeded(None)
        case Output(vals)    => Succeeded(Some(vals -> unit))
        case ff: Fail        => ff
        case it: Interrupted => it
        case _               => Uncons(self)
      } 
  }
```


```scala

private[fs2] def uncons: Pull[F, Nothing, Option[(Chunk[O], Pull[F, O, Unit])]] =
      self match {
        case Succeeded(_)    => Succeeded(None)
        case Output(vals)    => Succeeded(Some(vals -> unit))
        case ff: Fail        => ff
        case it: Interrupted => it
        case _               => Uncons(self)
      } 
  //we move from the above in StreamPullOps to the below in ToPull 
 def uncons: Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]] =
      self.underlying.uncons.map(_.map { case (hd, tl) => (hd, tl.streamNoScope) })


//return type `Pull[F, Nothing, Option[(Chunk[O], Pull[F, O, Unit])]]` to `Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]]`

// Nothing as ouput as it cannot emit any value
```
The returned value is an `Option` because the `Stream` may be empty: If there is no more value in the original `Stream`, we will have a `None`. Otherwise, we will have the head of the stream as a `Chunk` and a `Stream` representing the tail of the original stream.

Due to the structure of the type, the functions implemented using the `Pull` type are often recursive.

 A critical feature of running two streams through the concurrently method is that the second stream halts when the first stream is finished

 We can output multiple elements by composing pulls together using the `>>` function, spoken as ‘then’.

 The `Stream[F, O]` datatype has two type parameters: an effect `F` and an output type `O`. On the other hand `Pull[F, O, R]` has three. What is that extra `R` type for?

Unlike streams, pulls have a result.

A pull can only be represented as a stream if it has a result of Unit — meaning its result can be discarded.

`Pull[F, O, R]` is a functor over the type `R`, representing the result of the computation.

`Stream[F, A]` is a stream with effects in `F` and elements in `A`. `Pull[F, A, R]` is a pull with effects in `F`, elements in `A`, and a return value in `R`. Streams are implemented in terms of Pull[F, A, Unit] (more or less; handwaving some details). `Streams` are monads in `A`, meaning they have `flatMap(f: A => Stream[F, B]): Stream[F, B]`. `Pull` is a monad in `R`, so it has `flatMap(f: R => Pull[F, A, R2]): Pull[F, A, R2]`



![Alt text](image.png)
[Pull](https://gist.io/@daenyth/024c5584da01acabe7a435c8a53c4f3c)
[system-from-scratch-in-scala-3](https://chollinger.com/blog/2023/06/building-a-functional-effectful-distributed-system-from-scratch-in-scala-3-just-to-avoid-leetcode-part-1/)
[fold](https://www.baeldung.com/scala/folding-lists)

[pull](https://kebab-ca.se/chapters/fs2/overview.html)

http://www.gibbons.org.uk/scala3-fs2-july-2021

https://github.com/PendaRed/scala3-fs2/tree/main/src/main/scala/com/jgibbons/fs2/c

[free monad-haskell](https://serokell.io/blog/introduction-to-free-monads)








