name := "abstore"

organization := "fr.eurecom"

version := "0.1.0"

scalaVersion := "2.10.4"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
	"io.ckite" % "ckite" % "0.1.6",
  "com.github.zk1931" % "jzab" % "0.3.0",
	"com.twitter" %% "finagle-http" % "6.6.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.1.3",
  "ch.qos.logback" % "logback-classic" % "1.1.1",
  "com.github.scopt" %% "scopt" % "3.2.0"
)

EclipseKeys.withSource := true

net.virtualvoid.sbt.graph.Plugin.graphSettings

fork in run := false

unmanagedSourceDirectories in Compile <++= baseDirectory { base =>
  Seq(
    base / "src/main/resources"
  )
}
