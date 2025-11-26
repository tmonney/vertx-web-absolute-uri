package org.acme.absolute_uri;

import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("")
public class HelloResource {

    @Inject
    HttpServerRequest serverRequest;

    @GET
    @Path("hello")
    public String hello() {
        return "Absolute URI: " + serverRequest.absoluteURI();
    }
}
