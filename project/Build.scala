import sbt._
import sbt.Keys._

import bintray.Plugin._
import bintray.Keys._

object Build extends Build {

  val customBintraySettings = bintrayPublishSettings ++ Seq(
    packageLabels in bintray       := Seq("jws"),
    bintrayOrganization in bintray := Some("plasmaconduit"),
    repository in bintray          := "releases"
  )

  val root = Project("root", file("."))
    .settings(customBintraySettings: _*)
    .settings(
      name                := "jws",
      organization        := "com.plasmaconduit",
      version             := "0.10.0",
      scalaVersion        := "2.11.2",
      licenses            += ("MIT", url("http://opensource.org/licenses/MIT")),
      resolvers           += "Plasma Conduit Repository" at "http://dl.bintray.com/plasmaconduit/releases",
      libraryDependencies += "com.plasmaconduit" %% "json" % "0.5.0",
      libraryDependencies += "com.plasmaconduit" %% "jwa" % "0.6.0"
    )

}
