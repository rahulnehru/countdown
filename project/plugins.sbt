logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.19")

addSbtPlugin("com.heroku" % "sbt-heroku" % "2.1.2")
