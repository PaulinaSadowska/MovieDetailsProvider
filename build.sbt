name := "MovieDetailsProvider"

version := "1.0"

scalaVersion := "2.11.6"

// https://mvnrepository.com/artifact/com.typesafe.play/play-ws_2.11
libraryDependencies += "com.typesafe.play" % "play-ws_2.11" % "2.5.14"
libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.3.175"
)