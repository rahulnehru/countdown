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

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % Test

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

// exposing the play ports
dockerExposedPorts := Seq(9000, 9443)
dockerEntrypoint := Seq(
  "bin/countdown",
  "-Dconfig.resource=prod.conf",
  "-Dlogger.resource=logback-prod.xml")
// run using docker run -p 9000:9000 --rm countdown:1.0