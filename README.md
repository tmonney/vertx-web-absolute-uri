# vertx-web-absolute-uri

A reproducer for an issue with Vert.x Web where the absolute URI is not computed correctly when the target request is
absolute. For example, with this request:
```http request
GET http://localhost:%d/hello HTTP/1.1
Host: some.random.host.com:9876
```

`io.vertx.ext.web.impl.HttpServerRequestWrapper#absoluteURI()` returns `http://some.random.host.com:9876http://localhost:%d/hello`,
which is obviously wrong.

The reproducer uses Quarkus for demonstration purposes, but the problem can be pinpointed in `io.vertx.ext.web.impl.ForwardedParser#calculate()`:

```java
if (host != null) {
  this.authority = HostAndPort.create(host, port);
  host = host + (port >= 0 ? ":" + port : "");
  absoluteURI = scheme + "://" + host + delegate.uri();
}
```
Here `delegate.uri()` is already absolute: in the test it's `http://localhost:8080/hello`, so the simple concatenation above gives the wrong result.

To run the reproducer, one can simply execute `mvn test`.
