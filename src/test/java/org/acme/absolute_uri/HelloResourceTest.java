package org.acme.absolute_uri;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.core.Promise;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class HelloResourceTest {

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "quarkus.http.test-port")
    int httpPort;

    @Test
    void absoluteUri_hostMismatch() {
        // given
        var request = """
                GET http://localhost:%d/hello HTTP/1.1\r
                Host: some.random.host.com:9876\r
                \r
                """.formatted(httpPort);

        // when
        var response = send(request);

        // then
        // we would expect the URI to be one of
        // * http://some.random.host:9876/hello
        // * http://localhost:8080/hello
        assertEquals("""
                HTTP/1.1 200 OK\r
                content-length: 74\r
                Content-Type: text/plain;charset=UTF-8\r
                \r
                Absolute URI: http://some.random.host.com:9876http://localhost:%d/hello""".formatted(httpPort), response);
    }

    @Test
    void absoluteUri_matchingHosts() {
        // given
        var request = """
                GET http://localhost:%d/hello HTTP/1.1\r
                Host: localhost:%d\r
                \r
                """.formatted(httpPort, httpPort);


        // when
        var response = send(request);

        // then
        // we would expect the URI to be http://localhost:8080/hello
        assertEquals("""
                HTTP/1.1 200 OK\r
                content-length: 64\r
                Content-Type: text/plain;charset=UTF-8\r
                \r
                Absolute URI: http://localhost:%dhttp://localhost:%d/hello""".formatted(httpPort, httpPort), response);
    }

    private String send(String request) {
        Promise<String> promise = Promise.promise();
        var netClient = vertx.createNetClient();
        netClient.connect(httpPort, "localhost")
                .call(socket -> {
                    socket.writeAndForget(request);

                    socket.handler(response -> {
                        socket.closeAndForget();
                        netClient.closeAndForget();
                        promise.complete(response.toString());
                    });
                    socket.endHandler(promise::tryComplete);
                    socket.exceptionHandler(promise::tryFail);
                    return promise.future();
                })
                .await().indefinitely();
        return promise.futureAndAwait();
    }
}