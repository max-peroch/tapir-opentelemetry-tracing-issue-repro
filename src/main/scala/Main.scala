import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.{IpAddress, Port}
import com.typesafe.scalalogging.StrictLogging
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.{Context, IOLocalContextStorage}
import org.typelevel.otel4s.trace.Tracer
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.tracing.otel4s.Otel4sTracing
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

  private def serverOptions(tracer: Tracer[IO]): Http4sServerOptions[IO] =
    Http4sServerOptions
      .customiseInterceptors[IO]
      .prependInterceptor(Otel4sTracing(tracer))
      .options

  private def server(tracer: Tracer[IO]): Resource[IO, org.http4s.server.Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(IpAddress.fromString("0.0.0.0").get)
      .withPort(Port.fromInt(8080).get)
      .withHttpApp(routes(serverOptions(tracer)).orNotFound)
      .build

  override def run(args: List[String]): IO[ExitCode] = {

    implicit val provider: LocalProvider[IO, Context] =
      IOLocalContextStorage.localProvider[IO]

    val program = for {
      otel <- OtelJava.autoConfigured[IO]()
      tracer <- Resource.eval(otel.tracerProvider.get("test-tracer"))
      _ <- server(tracer)
    } yield ()
    program.useForever.as(ExitCode.Success)
  }
}
