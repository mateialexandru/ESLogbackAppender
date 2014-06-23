import sbt._
import Keys._
import scala._

import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc
import spray.revolver.RevolverPlugin.Revolver
import com.github.retronym.SbtOneJar

object logbackLogstashBuild extends Build {

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.0" withSources()
  val scalaspecs =  "org.specs2" %% "specs2" % "1.14" % "test" withSources()
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.9"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.4.0" exclude("com.typesafe.akka","akka-actor_2.10")
  val jodaTime = "joda-time" % "joda-time" % "2.1"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"
  val grizzled = "org.clapper" %% "grizzled-slf4j" % "1.0.2"
  val pegdown = "org.pegdown" % "pegdown" % "1.0.2"
  val testKit = "io.spray" % "spray-testkit" % "1.3.1" % "test" withSources()
  val junit = "junit" % "junit" % "4.11" % "test" withSources()
  val slick = "com.typesafe.slick" %% "slick" % "2.1.0-M2" withSources()
  val elasticsearch = "org.elasticsearch" % "elasticsearch" % "1.2.1" withSources()
  val metrics = "com.codahale.metrics" % "metrics-core" % "3.0.1" withSources()
  val scalaActors = "org.scala-lang" % "scala-actors" % "2.11.1" withSources()
  def defaultSettings =
    Project.defaultSettings ++
    SbtOneJar.oneJarSettings ++    
    publishSettings ++ 
    Seq(
      organization := "com.busymachines",
      version := "1.0.0-SNAPSHOT",
      sbtPlugin := false,
      scalaVersion := "2.11.1",
crossScalaVersions := Seq("2.11.1", "2.10.4"),
      publishMavenStyle := false,
      exportJars := true,      
      parallelExecution in Global := false,

        scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "utf8", "-feature", "-language:implicitConversions", "-language:postfixOps", "-language:higherKinds", "-language:existentials", "-language:reflectiveCalls"),
            scalacOptions <++= scalaBinaryVersion map {
              case "2.11" => Seq("-Ydelambdafy:method")
              case _ => Nil
            },


      javacOptions in Compile ++= Seq("-encoding", "utf8", "-g"),
      EclipseKeys.withSource := true,
      EclipseKeys.skipParents in ThisBuild := true, // true is the default
      EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
      libraryDependencies ++= Seq(scalatest),
	  javaOptions in Test += "-Dcom.busymachines.loglevel=all",
	  fork in test := true,
	  testOptions in Test += Tests.Argument("-oD"),
      testOptions in Test += Tests.Argument("-h","target/html-test-report"),
      testOptions in Test <+= (target in Test) map {
        t => Tests.Argument(TestFrameworks.ScalaTest, "junitxml(directory=\"%s\")" format (t / "test-reports"))
      },
 resolvers += Resolver.url("busymachines snapshots", url("http://archiva.busymachines.com/repository/snapshots/"))(Resolver.ivyStylePatterns),
        resolvers += "Typesafe Maven Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
        resolvers +=  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
        resolvers +=  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
        resolvers += "Typesafe Maven Releases" at "http://repo.typesafe.com/typesafe/releases/",
        resolvers += "spray repo" at "http://repo.spray.io"      

)

  def publishSettings = Seq(
    licenses := Seq("The Apache Software Licence, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/busymachines/logback-logstash")),
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials_busymachines_snapshots"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials_busymachines_releases"),
    publishTo <<= version { (v: String) =>
          val nexus = "http://archiva.busymachines.com"
          if (v.trim.endsWith("SNAPSHOT")) 
            Some(Resolver.url("snapshots", new URL(nexus + "/repository/snapshots/"))(Resolver.ivyStylePatterns))
          else
            Some(Resolver.url("snapshots", new URL(nexus + "/repository/releases/"))(Resolver.ivyStylePatterns))
        })



  lazy val logbackLogstash = Project(id = "logback-logstash", base = file("logback-logstash"), settings = defaultSettings ++ Seq(
    libraryDependencies ++= Seq(grizzled, scalatest,junit,testKit,  logback,  jodaTime, jodaConvert, slick, elasticsearch, pegdown, metrics, scalaActors)))

}
