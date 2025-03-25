import sbt._

name := "otel-repro"
scalaVersion := "2.13.16"
libraryDependencySchemes += "org.typelevel" %% "otel4s-oteljava"  % VersionScheme.Always
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % "0.23.30",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.11.20",
  "com.softwaremill.sttp.tapir" %% "tapir-cats" % "1.11.20",
  "com.softwaremill.sttp.tapir" %% "tapir-otel4s-tracing" % "1.11.20",
  "org.typelevel" %% "cats-effect" % "3.6.0",
  "org.typelevel" %% "otel4s-oteljava" % "0.12.0",
  "org.typelevel" %% "otel4s-oteljava-context-storage" % "0.12.0",
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.48.0" % Runtime,
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "1.48.0" % Runtime,
  "io.opentelemetry.instrumentation" % "opentelemetry-logback-mdc-1.0" % "2.14.0-alpha" % Runtime,
  "ch.qos.logback" % "logback-classic" % "1.5.18",
  "net.logstash.logback" % "logstash-logback-encoder" % "8.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
)
run / fork := true
run / javaOptions := {
  Seq(
    "-Dcats.effect.trackFiberContext=true",
    "-Dotel.service.name=otel-repro",
    "-Dotel.java.global-autoconfigure.enabled=true",
    "-Dotel.propagators=tracecontext,baggage",
    "-Dotel.logs.exporter=none",
    "-Dotel.metrics.exporter=none",
    "-Dotel.trace.exporter=otlp",
    "-Dotel.instrumentation.logback-mdc.add-baggage=true",
    "-Dlogback.configurationFile=logback.xml"
  )
}
