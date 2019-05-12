import sbt.Keys.libraryDependencies

val appName = "countdown"

name := appName
 
version := "1.0" 
      
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

herokuAppName in Compile := Map(
  "prod"  -> "dd-standups-server",
  "stage" -> "dd-standups-server-stage"
).getOrElse(sys.props("appEnv"), "dd-standups-server-stage")

herokuIncludePaths in Compile := Seq(
  "standups.json"
)
herokuProcessTypes in Compile := Map(
  "web" -> "target/universal/stage/bin/countdown -Dhttp.port=$PORT -Dconfig.resource=prod.conf"
)

val dockerTestkitLibs = Seq(
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.8",
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.8",
  "com.whisk" %% "docker-testkit-config" % "0.9.8"
)

val testLibraryDependencies = (
  Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2",
    "org.mockito" %% "mockito-scala" % "1.4.0-beta.7",
    "org.mockito" %% "mockito-scala-scalatest" % "1.4.0-beta.7"
  ) ++ dockerTestkitLibs
  ).map(_ % "test, it")

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

val ITest = config("it") extend(Test)
scalaSource in ITest := baseDirectory.value / "/it"

lazy val `countdown` = (project in file(".")).enablePlugins(PlayScala, UniversalPlugin, JavaAppPackaging, DockerPlugin)
  .enablePlugins(PlayScala)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(unmanagedResourceDirectories in IntegrationTest += baseDirectory.value / "it" / "resources")
  .settings(ITest / parallelExecution := false )


// exposing the play ports
dockerExposedPorts := Seq(9000, 9443)
dockerEntrypoint := Seq(
  "bin/countdown",
  "-Dconfig.resource=prod.conf",
  "-Dlogger.resource=logback-prod.xml")
// run using docker run -p 9000:9000 --rm countdown:1.0