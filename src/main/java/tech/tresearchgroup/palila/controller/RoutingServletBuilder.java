package tech.tresearchgroup.palila.controller;

import io.activej.http.RoutingServlet;
import tech.tresearchgroup.palila.model.endpoints.Endpoint;

public class RoutingServletBuilder {
    public static RoutingServlet build(Endpoint[] endpoints) {
        RoutingServlet routingServlet = RoutingServlet.create();
        for (Endpoint endpoint : endpoints) {
            routingServlet.map(endpoint.getMethod(), endpoint.getUrl(), endpoint.getServlet());
        }
        return routingServlet;
    }
}
