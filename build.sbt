import AssemblyKeys._ 

organization := name.value

assemblySettings

mainClass in assembly := Some("showmyns.MainClass")

mainClass in (Compile,run) := Some("showmyns.MainClass")

jarName in assembly := "showMy1.jar"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M8" % "test"

resolvers += Resolver.sonatypeRepo("snapshots")

EclipseKeys.withSource := true

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.5"
