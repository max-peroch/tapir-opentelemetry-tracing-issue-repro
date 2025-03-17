import sbt._

name := "otel-repro"
scalaVersion := "2.13.16"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % "0.23.29",
  "org.http4s" %% "http4s-ember-client" % "0.23.29",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.11.19",
  "com.softwaremill.sttp.tapir" %% "tapir-cats" % "1.11.19",
  "com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-tracing" % "1.11.18", // TODO "1.11.19" breaks otel propagation
  "org.typelevel" %% "cats-effect" % "3.6.0-RC2",
  "org.typelevel" %% "otel4s-oteljava" % "0.12.0-RC3",
  "org.typelevel" %% "otel4s-oteljava-context-storage" % "0.12.0-RC3",
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.48.0" % Runtime,
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "1.48.0" % Runtime,
  "io.opentelemetry.instrumentation" % "opentelemetry-logback-mdc-1.0" % "2.13.3-alpha" % Runtime,
  "ch.qos.logback" % "logback-classic" % "1.5.12",
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
