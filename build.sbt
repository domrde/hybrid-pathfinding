name := "hybrid-pathfinding"

version := "1.0"

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.12.1",
  organization := "com.dda"
)

lazy val common = (project in file("common"))
  .settings(
    name := "common",
    libraryDependencies ++= {
      Seq(
        "org.scala-js" % "scalajs-stubs_2.12" % "0.6.14" % "provided"
      )
    }
  )
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)

lazy val server = (project in file("server"))
  .settings(commonSettings: _*)
  .settings(
    name := "server",
    libraryDependencies ++= {
      val akkaV = "2.4.17"
      val akkaHttpV = "10.0.3"
      Seq(
        "tw.edu.ntu.csie" % "libsvm" % "3.17",
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-stream" % akkaV,
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV
      )
    })
  .dependsOn(common)

lazy val client = (project in file("client"))
  .settings(commonSettings: _*)
  .settings(
    name := "client",
    libraryDependencies ++= {
      Seq(
        "org.scala-js" %% "scalajs-dom_sjs0.6" % "0.9.1",
        "com.lihaoyi" %% "upickle_sjs0.6" % "0.4.4"
      )
    })
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(common)
