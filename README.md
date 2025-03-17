# Minimal reproduction project for the Otel4s propagation breakage

Run `sbt run` to get the server running. The server will be running on `localhost:8080`.

Then `curl localhost:8080/ping` in a different terminal, it will return `pong`.

The server logs will be printed in the terminal where the server is running and should show the following:

```shell     
[info] INFO  Main$ - [trace_id=f1b40cb7f48471070f64edf33904c234, trace_flags=01, span_id=25d8790e50633535] - should see see the trace_id/span_id in the MDC
```

Now change the version of `"com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-tracing" % "1.11.18"` to `1.11.19` in `build.sbt` and run `sbt run` again.

Then curl the ping endpoint again, the server will return `pong` but the logs will not show the trace_id/span_id as it did before.

```shell
[info] INFO  Main$ - [] - should see see the trace_id/span_id in the MDC
```
