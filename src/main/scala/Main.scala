import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.{IpAddress, Port}
import com.typesafe.scalalogging.StrictLogging
import io.opentelemetry.api.OpenTelemetry
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.otel4s.oteljava.OtelJava
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.tracing.opentelemetry.OpenTelemetryTracing
import sttp.tapir.{endpoint, stringBody}

object Main extends IOApp with StrictLogging {

  private val pingEndpoint = endpoint.get
    .in("ping")
    .out(stringBody)

  private val pingServerEndpoint: ServerEndpoint[Any with Fs2Streams[IO], IO] =
    pingEndpoint
      .serverLogic[IO] { _ =>
        logger.info("should see see the trace_id/span_id in the MDC")
        IO.pure(Right("pong"))
      }

  private def routes(serverOptions: Http4sServerOptions[IO]): HttpRoutes[IO] =
    Http4sServerInterpreter(serverOptions).toRoutes(List(pingServerEndpoint))

  private def serverOptions(otel: OpenTelemetry): Http4sServerOptions[IO] =
    Http4sServerOptions
      .customiseInterceptors[IO]
      .prependInterceptor(OpenTelemetryTracing(otel))
      .options

  private def server(
      otel: OpenTelemetry
  ): Resource[IO, org.http4s.server.Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(IpAddress.fromString("0.0.0.0").get)
      .withPort(Port.fromInt(8080).get)
      .withHttpApp(routes(serverOptions(otel)).orNotFound)
      .build

  override def run(args: List[String]): IO[ExitCode] = {

    val program = for {
      otel <- OtelJava.autoConfigured[IO]()
      _ <- server(otel.underlying)
    } yield ()
    program.useForever.as(ExitCode.Success)
  }
}
