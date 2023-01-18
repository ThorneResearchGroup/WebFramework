package tech.tresearchgroup.palila.controller.endpoints;

import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;

public interface GenericMediaTypeControllerInterface {
    HttpResponse get(int page, int pageSize, HttpRequest httpRequest);

    HttpResponse post(String data, HttpRequest httpRequest);

    HttpResponse put(String data, HttpRequest httpRequest);

    HttpResponse getSample(HttpRequest httpRequest);

    HttpResponse getById(Long albumId, HttpRequest httpRequest);

    HttpResponse patch(String data, HttpRequest httpRequest);

    HttpResponse deleteById(Long albumId, HttpRequest httpRequest);

    HttpResponse deleteIndexes(HttpRequest httpRequest);

    HttpResponse databaseSearch(String query, String returnColumn, HttpRequest httpRequest);
}
