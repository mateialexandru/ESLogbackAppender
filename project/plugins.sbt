import sbt._
import Defaults._

resolvers += "gseitz@github" at "http://gseitz.github.com/maven/"

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")



