package tech.tresearchgroup.palila.controller;

import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import tech.tresearchgroup.palila.model.Card;
import tech.tresearchgroup.palila.model.enums.ReturnType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface GenericControllerInterface {
    Object createSecureResponse(Object object, ReturnType returnType, HttpRequest httpRequest) throws Exception;

    Object readSecureResponse(long id, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException;

    Object readPaginatedResponse(int page, int pageSize, boolean full, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException;

    boolean update(long id, Object object, HttpRequest httpRequest) throws Exception;

    boolean delete(long id, HttpRequest httpRequest) throws Exception;

    Object search(String query, String returnColumn, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException;

    Object readOrderByPaginated(int resultCount, int page, String orderBy, boolean ascending, boolean full, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException;

    Long getTotal(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException;

    Long getTotalPages(int maxResultsSize, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException;

    HttpResponse deleteAllIndexes(HttpRequest httpRequest) throws Exception;

    boolean reindex(HttpRequest httpRequest) throws Exception;

    Object getSample(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException;

    Card toCard(Object object, String action) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
}
