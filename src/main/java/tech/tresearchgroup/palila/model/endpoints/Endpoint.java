package tech.tresearchgroup.palila.model.endpoints;

import io.activej.http.AsyncServlet;
import io.activej.http.HttpMethod;

public class Endpoint {
    private HttpMethod method;
    private String url;
    private AsyncServlet servlet;
    private String[] tags;
    private EndpointParameter[] parameters;
    private EndpointResponse[] responses;

    public Endpoint(HttpMethod method, String url, AsyncServlet servlet) {
        this.method = method;
        this.url = url;
        this.servlet = servlet;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AsyncServlet getServlet() {
        return servlet;
    }

    public void setServlet(AsyncServlet servlet) {
        this.servlet = servlet;
    }
}
