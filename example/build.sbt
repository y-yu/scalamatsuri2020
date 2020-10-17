val scala213Version = "2.13.3"
val defaultScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-Xlint",
  "-language:implicitConversions", "-language:higherKinds", "-language:existentials",
  "-unchecked"
)

lazy val root = (project in file("."))
  .settings(
    organization := "com.github.y-yu",
    name := "scalamatsuri2020",
    version := "0.1",
    description := "PoC of `An interpreter handling over effects for Eff`",
    homepage := Some(url("https://github.com/y-yu")),
    licenses := Seq("MIT" -> url(s"https://github.com/y-yu/scalamatsuri2020/blob/master/LICENSE")),
    scalaVersion := scala213Version,
    scalacOptions ++= defaultScalacOptions,
    libraryDependencies ++= Seq(
      "org.atnos" %% "eff" % "5.12.0",
      "org.scalatest" %% "scalatest" % "3.2.2" % "test",
      "org.scalikejdbc" %% "scalikejdbc"       % "3.5.0",
      "org.scalikejdbc" %% "scalikejdbc-config" % "3.5.0",
      "com.h2database"  %  "h2" % "1.4.200",
      "com.lihaoyi" %% "pprint" % "0.6.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "ch.qos.logback"  %  "logback-classic"   % "1.2.3"
      //"org.atnos" %% "eff-monix" % "5.12.0" % "test"
    )
  )
