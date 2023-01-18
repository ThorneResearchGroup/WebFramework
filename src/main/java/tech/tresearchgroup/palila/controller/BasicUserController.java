package tech.tresearchgroup.palila.controller;

import com.google.gson.Gson;
import com.meilisearch.sdk.Client;
import com.zaxxer.hikari.HikariDataSource;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.serializer.BinarySerializer;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.enums.CacheTypesEnum;
import tech.tresearchgroup.palila.model.enums.PermissionGroupEnum;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public class BasicUserController extends BaseController implements GenericControllerInterface {

    public BasicUserController(HikariDataSource hikariDataSource,
                               Gson gson,
                               Client client,
                               Class theClass,
                               int reindexSize,
                               BinarySerializer serializer,
                               String searchColumn,
                               Object sample,
                               PermissionGroupEnum createPermissionLevel,
                               PermissionGroupEnum readPermissionLevel,
                               PermissionGroupEnum updatePermissionLevel,
                               PermissionGroupEnum deletePermissionLevel,
                               PermissionGroupEnum searchPermissionLevel) throws Exception {
        super(hikariDataSource, gson, client, theClass, serializer, reindexSize, searchColumn, sample);
    }

    @Override
    public Object createSecureResponse(Object object, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (genericDAO.create(object)) {
            genericPageCAO.delete();
            return object;
        }
        return null;
    }

    @Override
    public byte[] createSecureAPIResponse(Object object, HttpRequest httpRequest) {
        return new byte[0];
    }

    @Override
    public byte[] readSecureAPIResponse(long id, HttpRequest httpRequest) {
        return new byte[0];
    }

    @Override
    public Object readSecureResponse(long id, HttpRequest httpRequest) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        byte[] cachedData = genericLocalCAO.read(CacheTypesEnum.DATABASE, id);
        Object object;
        if (cachedData != null) {
            if (BaseSettings.debug) {
                System.out.println("Cache hit: " + "/v1/" + simpleName);
            }
            object = ActiveJSerializer.deserialize(cachedData, serializer);
        } else {
            if (BaseSettings.debug) {
                System.out.println("Cache miss: " + "/v1/" + simpleName);
            }
            object = genericDAO.read(id, theClass);
            if (object != null) {
                if (BaseSettings.cacheEnable) {
                    if (BaseSettings.debug) {
                        System.out.println("Adding to cache: " + object);
                    }
                    byte[] binary = ActiveJSerializer.serialize(object, serializer);
                    genericLocalCAO.create(CacheTypesEnum.DATABASE, id, binary);
                }
            } else {
                if (BaseSettings.debug) {
                    System.out.println("Failed to load: " + theClass.getSimpleName() + " : " + id);
                }
            }
        }
        return object;
    }

    @Override
    public List readPaginatedResponse(int page, int pageSize, boolean full, HttpRequest httpRequest) throws SQLException {
        return genericDAO.readPaginated(pageSize, page, theClass, full);
    }

    @Override
    public byte[] readPaginatedAPIResponse(int page, int pageSize, boolean full, HttpRequest httpRequest) {
        return new byte[0];
    }

    @Override
    public boolean update(long id, Object object, HttpRequest httpRequest) throws Exception {
        if (genericDAO.update(object)) {
            byte[] json = gson.toJson(object).getBytes();
            byte[] binary = ActiveJSerializer.serialize(object, serializer);
            genericLocalCAO.update(CacheTypesEnum.DATABASE, id, binary);
            byte[] compressed = CompressionController.compress(json);
            genericLocalCAO.update(CacheTypesEnum.API, id, compressed);
            genericPageCAO.delete();
            genericSAO.updateDocument(object, index);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(long id, HttpRequest httpRequest) throws Exception {
        if (genericDAO.delete(id, theClass)) {
            genericLocalCAO.delete(id);
            genericPageCAO.delete();
            genericSAO.deleteDocument(id, index);
            return true;
        }
        return false;
    }

    @Override
    public List search(String query, String returnColumn, HttpRequest httpRequest) throws SQLException {
        return genericDAO.databaseSearch(BaseSettings.maxSearchResults, query, returnColumn, SEARCH_COLUMN, theClass);
    }

    @Override
    public byte[] searchAPIResponse(String query, String returnColumn, HttpRequest httpRequest) {
        return new byte[0];
    }

    @Override
    public List readOrderByPaginated(int resultCount, int page, String orderBy, boolean ascending, boolean full, HttpRequest httpRequest) {
        return null;
    }

    @Override
    public byte[] readOrderByPaginatedAPI(int resultCount, int page, String orderBy, boolean ascending, boolean full, HttpRequest httpRequest) {
        return new byte[0];
    }

    @Override
    public Long getTotal(HttpRequest httpRequest) throws SQLException {
        return genericDAO.getTotal(theClass);
    }

    @Override
    public Long getTotalPages(int maxResultsSize, HttpRequest httpRequest) throws SQLException {
        return genericDAO.getTotalPages(maxResultsSize, theClass);
    }

    @Override
    public HttpResponse deleteAllIndexes(HttpRequest httpRequest) {
        return null;
    }

    @Override
    public boolean reindex(HttpRequest httpRequest) {
        try {
            genericSAO.reindex(BaseSettings.maxSearchResults, genericDAO, index, theClass);
        } catch (Exception e) {
            if (BaseSettings.debug) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    @Override
    public byte[] getSample(HttpRequest httpRequest) {
        return sample;
    }
}
