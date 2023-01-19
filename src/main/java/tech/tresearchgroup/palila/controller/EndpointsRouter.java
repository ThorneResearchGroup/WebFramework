package tech.tresearchgroup.palila.controller;

import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import tech.tresearchgroup.palila.model.endpoints.Endpoint;

public interface EndpointsRouter {
    @Provides
    RoutingServlet servlet();

    Endpoint[] getEndpoints();
}
