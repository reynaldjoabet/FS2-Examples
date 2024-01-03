import scala.concurrent.Future
import fs2.Pure
import cats.instances.stream
import cats.effect.IO
import fs2._

val unfold1=fs2.Stream.unfold(1)(x=>Some(x->(x-1)))
val unfold=fs2.Stream.unfold(0)(i=> if(i <20) Some(i->(i+1)) else None)

unfold.toList

val emit=fs2.Stream.emit(12)
emit.toList
val empty=fs2.Stream.empty
empty.toList

val chunk= fs2.Stream.unfoldChunk(0)(i => if (i < 5) Some(Chunk.seq(List.fill(i)(i)) -> (i+1)) else None)
chunk.toList

val emit1=fs2.Stream.emit(Chunk.seq((1 to 20)))

emit1.evalMap(c=> IO(c.toList)).compile.toList


val io=IO(12)

fs2.Stream.unfoldLoop(0)(i => (i, if (i < 5) Some(i+1) else None)).toList


fs2.Stream.emits('A' to 'E').toList

fs2.Stream.emits[IO, Char]('A' to 'E')
  .map(letter => fs2.Stream.emits[IO, Int](1 to 3).map(index => s"$letter$index"))
  .parJoin(5)

  val s = fs2.Stream(1, 2) ++ fs2.Stream(3) ++ fs2.Stream(4, 5, 6)
  s.chunks.toList
  fs2.Stream(1, 2, 3).append(fs2.Stream(4, 5, 6)).mapChunks { c => val ints = c.toArraySlice; for (i <- 0 until ints.values.size) ints.values(i) = 0; ints }.toList

  fs2.Stream("Hello", "Hi", "Greetings", "Hey").groupAdjacentBy(_.head).toList.map { case (k,vs) => k -> vs.toList }



  def lettersIter: fs2.Stream[Pure,Char]=fs2.Stream.emits('A' to 'Z')

  lettersIter.toList

  def lettersUnfold=fs2.Stream.unfold('a')(let=>Some(let,(let+1).toChar))

  lettersUnfold.take(26).toList

  val numbers=fs2.Stream.iterate(0)(_+1)

  numbers.take(12).toList

  def myIterate[A](initial:A)(next:A=>A): fs2.Stream[Pure,A]= fs2.Stream.unfold(initial)(init=>Some(init->next(init)))

  myIterate(0)(_+1).take(12).toList



  def repeat[A](stream:fs2.Stream[Pure,A]): fs2.Stream[Pure,A]=stream.repeat


  repeat(fs2.Stream.emits(1 to 5)).take(10).toList

 def unNone[A](stream:fs2.Stream[Pure,Option[A]]): fs2.Stream[Pure,A]=stream.flatMap{
  case None => fs2.Stream.empty
  case Some(value) => fs2.Stream.emit(value)
 }

def unNone1[A](stream:fs2.Stream[Pure,Option[A]]): fs2.Stream[Pure,A]= stream.collect{case Some(value)=>value}

def unNone2[A](stream:fs2.Stream[Pure,Option[A]]): fs2.Stream[Pure,A]= stream.flatMap(opt=>fs2.Stream.fromOption(opt))
val k=fs2.Stream.unfold(Some(12))(num=>Some(num,Some(num.get*2)))
 unNone(k).take(6).toList

 unNone1(fs2.Stream(Some(12),None,None,Some(2),None)).toList

 
 unNone2(fs2.Stream(Some(12),None,None,Some(2),None,Some(89))).toList


 fs2.Stream.constant(2).zipWith(fs2.Stream.iterate(1) (_+1))(_*_).take(12).toList.groupBy(_%2==0)
import scala.concurrent.ExecutionContext.Implicits.global
 val h=Future(8)
  Future.sequence(List(h))

(Stream(1,2,3) ++ Stream(4,5,6)).toList

  val list=(1 to 10).toList.partition(_%2==0)
  list
import cats.effect.SyncIO
 (Stream(1,2,3) ++ Stream.raiseError[SyncIO](new RuntimeException) ++ Stream(4,5,6)).attempt.compile.toList.unsafeRunSync()
val list1=(1 to 190).toList.groupBy(_%2==0)
list1

unfold1.take(120000).toList