// import cats.effect._
// import cats.effect.unsafe.IORuntime

// class Allocator(implicit runtime: IORuntime) {
//   // Ref that will keep track of finalizers
//   private val shutdown: Ref[IO, IO[Unit]] = Ref.unsafe(IO.unit)

//   // Method to allocate dependencies
//   def allocate[A](resource: Resource[IO, A]): A =
//     resource.allocated.flatMap { case (a, release) =>
//       // Shutdown this resource, and after shutdown all previous
//       shutdown.update(release *> _).map(_ => a)
//   }.unsafeRunSync()

//   // Shutdown dependencies
//   def shutdownAll: IO[Unit] = {
//     shutdown.getAndSet(IO.unit).flatten
//   }

// }

// final case class Repository(name:String)

// final case class ServiceA(repo:Repository)
// final case class ServiceB(repo:Repository)

// final  case class HttpServerTask(serviceA:ServiceA,serviceB:ServiceB)


// class Dependencies(val allocator: Allocator) {
//   lazy val conn = allocator.allocate {
//     Resource.pure(3)
//   }

//   lazy val repo = new Repository(???)

//   lazy val serviceA = new ServiceA(repo)

//   lazy val serviceB = new ServiceB(repo)

//   lazy val server = HttpServerTask(serviceA, serviceB)
// }