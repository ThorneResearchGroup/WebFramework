package tech.tresearchgroup.palila.controller;

import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public interface GenericControllerInterface {
    Object createSecureResponse(Object object, HttpRequest httpRequest) throws Exception;

    byte[] createSecureAPIResponse(Object object, HttpRequest httpRequest) throws Exception;

    Object readSecureResponse(long id, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    byte[] readSecureAPIResponse(long id, HttpRequest httpRequest) throws IOException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    List readPaginatedResponse(int page, int pageSize, boolean full, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    byte[] readPaginatedAPIResponse(int page, int pageSize, boolean full, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, InstantiationException;

    boolean update(long id, Object object, HttpRequest httpRequest) throws Exception;

    boolean delete(long id, HttpRequest httpRequest) throws Exception;

    List search(String query, String returnColumn, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    byte[] searchAPIResponse(String query, String returnColumn, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, InstantiationException;

    List readOrderByPaginated(int resultCount, int page, String orderBy, boolean ascending, boolean full, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    byte[] readOrderByPaginatedAPI(int resultCount, int page, String orderBy, boolean ascending, boolean full, HttpRequest httpRequest) throws IOException, SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    Long getTotal(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    Long getTotalPages(int maxResultsSize, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    HttpResponse deleteAllIndexes(HttpRequest httpRequest) throws Exception;

    boolean reindex(HttpRequest httpRequest) throws Exception;

    Object getSample(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;
}
