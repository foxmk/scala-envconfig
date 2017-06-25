lazy val `scala-envconfig` = project
  .in(file("."))
  .settings(
    name := "scala-envconfig",
    organization := "com.github.foxmk",
    version := "1.0",
    scalaVersion := "2.12.2",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.12.2",
    libraryDependencies += "org.scalatest"  %% "scalatest"    % "3.0.1" % Test
  )
