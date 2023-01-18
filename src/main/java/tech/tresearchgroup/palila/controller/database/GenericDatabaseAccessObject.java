package tech.tresearchgroup.palila.controller.database;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public interface GenericDatabaseAccessObject {
    boolean create(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    Object read(Long id, Class theClass) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException;

    List<Object> readAll(Class theClass, boolean full) throws SQLException;

    List readPaginated(int resultCount, int page, Class theClass, boolean full) throws SQLException;

    List readOrderedBy(int resultCount, int page, Class theClass, String orderedBy, boolean ascending, boolean full) throws SQLException;

    boolean update(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;

    boolean delete(long id, Class theClass) throws SQLException;

    Long getTotal(Class theClass) throws SQLException;

    Long getTotalPages(int maxResultsSize, Class theClass) throws SQLException;

    List databaseSearch(int maxResultsSize, String query, String returnColumn, String searchColumn, Class theClass) throws SQLException;
}
