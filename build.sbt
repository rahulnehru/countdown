import sbt.Keys.libraryDependencies

name := "countdown"
 
version := "1.0" 
      
lazy val `countdown` = (project in file(".")).enablePlugins(PlayScala, UniversalPlugin, JavaAppPackaging, DockerPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

herokuAppName in Compile := "dd-standups-server"
herokuIncludePaths in Compile := Seq(
  "standups.json"
)

val testLibraryDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.mockito" %% "mockito-scala" % "1.4.0-beta.7",
  "org.mockito" %% "mockito-scala-scalatest" % "1.4.0-beta.7"
).map(_ % Test)

val slick = Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.0.1",
  "com.github.tminglei" %% "slick-pg" % "0.17.2",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.17.2",
  "com.github.tminglei" %% "slick-pg_joda-time" % "0.17.2"
)

libraryDependencies ++= Seq(
  ehcache , ws , guice, evolutions,
  "org.typelevel" %% "cats-core" % "1.6.0",
  "org.postgresql" % "postgresql" % "42.2.5"//jdbc driver
) ++
  slick ++
  testLibraryDependencies

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

// exposing the play ports
dockerExposedPorts := Seq(9000, 9443)
dockerEntrypoint := Seq(
  "bin/countdown",
  "-Dconfig.resource=prod.conf",
  "-Dlogger.resource=logback-prod.xml")
// run using docker run -p 9000:9000 --rm countdown:1.0