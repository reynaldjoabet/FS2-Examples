// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.13"

name    := "FS2-EXAMPLES"
version := "1.0"
val fs2Version = "3.10.2"
libraryDependencies += "co.fs2" %% "fs2-core" % fs2Version

libraryDependencies += "co.fs2" %% "fs2-io" % fs2Version

// by default sbt run runs the program in the same JVM as sbt
//in order to run the program in a different JVM, we add the following
//fork in run := true
//“enables a compiler flag (partial unification) that is required by cats to infer types correctly”

//scalacOptions += "-Ypartial-unification"
