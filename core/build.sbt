name := "JetMQ"

version := "0.1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-Xlint",
  "-deprecation",
  "-unchecked",
  "-feature"
)

val kamonVersion = "0.5.1"
val akkaVersion = "2.4.0"
val akkaStreamVersion = "2.0-M2"

libraryDependencies ++= Seq(
  "io.kamon" %% "kamon-core" % kamonVersion,
  "io.kamon" %% "kamon-akka" % kamonVersion,
  "io.kamon" %% "kamon-statsd" % kamonVersion,
  "io.kamon" %% "kamon-log-reporter" % kamonVersion,
  "io.kamon" %% "kamon-system-metrics" % kamonVersion,
  "org.specs2" %% "specs2-core" % "3.6.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.scodec" %% "scodec-core" % "1.7.1",
  "org.scodec" %% "scodec-bits" % "1.0.9",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "com.typesafe.play" %% "play-json" % "2.4.0",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStreamVersion,
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % akkaStreamVersion,
  "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaStreamVersion
)

//-javaagent:~/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.8.6.jar
javaAgents += "org.aspectj" % "aspectjweaver" % "1.8.6" % "compile;test"

parallelExecution in Test := false

javaOptions in Test += "-Dkamon.auto-start=true"

enablePlugins(JavaAgent, JavaAppPackaging)
