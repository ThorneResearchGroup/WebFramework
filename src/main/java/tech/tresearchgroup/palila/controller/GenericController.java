package tech.tresearchgroup.palila.controller;

import com.google.gson.Gson;
import com.meilisearch.sdk.Client;
import com.zaxxer.hikari.HikariDataSource;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.serializer.BinarySerializer;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.BasicFormObject;
import tech.tresearchgroup.palila.model.SecurityLog;
import tech.tresearchgroup.palila.model.enums.CacheTypesEnum;
import tech.tresearchgroup.palila.model.enums.PermissionGroupEnum;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class GenericController extends BaseController implements GenericControllerInterface {
    private final PermissionGroupEnum CREATE_PERMISSION_LEVEL;
    private final PermissionGroupEnum READ_PERMISSION_LEVEL;
    private final PermissionGroupEnum UPDATE_PERMISSION_LEVEL;
    private final PermissionGroupEnum DELETE_PERMISSION_LEVEL;
    private final PermissionGroupEnum SEARCH_PERMISSION_LEVEL;
    private final BasicUserController basicUserController;

    public GenericController(HikariDataSource hikariDataSource,
                             Gson gson,
                             Client client,
                             Class theClass,
                             BinarySerializer serializer,
                             int reindexSize,
                             String searchColumn,
                             Object sample,
                             PermissionGroupEnum createPermissionLevel,
                             PermissionGroupEnum readPermissionLevel,
                             PermissionGroupEnum updatePermissionLevel,
                             PermissionGroupEnum deletePermissionLevel,
                             PermissionGroupEnum searchPermissionLevel,
                             BasicUserController basicUserController) throws Exception {
        super(hikariDataSource, gson, client, theClass, serializer, reindexSize, searchColumn, sample);
        this.CREATE_PERMISSION_LEVEL = createPermissionLevel;
        this.READ_PERMISSION_LEVEL = readPermissionLevel;
        this.UPDATE_PERMISSION_LEVEL = updatePermissionLevel;
        this.DELETE_PERMISSION_LEVEL = deletePermissionLevel;
        this.SEARCH_PERMISSION_LEVEL = searchPermissionLevel;
        this.basicUserController = basicUserController;
    }

    @Override
    public Object createSecureResponse(Object object, HttpRequest httpRequest) throws Exception {
        if (canAccess(httpRequest, CREATE_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("create", theClass.getSimpleName(), userId, apiKey);
                loggingDAO.create(securityLog);
            }
            if (genericDAO.create(object)) {
                genericSAO.createDocument(object, index);
                genericPageCAO.delete();
                return object;
            }
        }
        return null;
    }

    @Override
    public byte[] createSecureAPIResponse(Object object, HttpRequest httpRequest) throws Exception {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            Object createdObject = createSecureResponse(object, httpRequest);
            if (createdObject != null) {
                return CompressionController.compress(gson.toJson(createdObject).getBytes());
            }
        }
        return null;
    }

    @Override
    public Object readSecureResponse(long id, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("read", theClass.getSimpleName() + "-" + id, userId, apiKey);
                loggingDAO.create(securityLog);
            }
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
                if (BaseSettings.cacheEnable) {
                    if (object != null) {
                        byte[] binary = ActiveJSerializer.serialize(object, serializer);
                        genericLocalCAO.create(CacheTypesEnum.DATABASE, id, binary);
                    }
                }
            }
            return object;
        } else {
            return unauthorized();
        }
    }

    @Override
    public byte[] readSecureAPIResponse(long id, HttpRequest httpRequest) throws IOException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("read", theClass.getSimpleName() + "-" + id, userId, apiKey);
                loggingDAO.create(securityLog);
            }
            byte[] cacheData = genericLocalCAO.read(CacheTypesEnum.API, id);
            if (cacheData != null) {
                if (BaseSettings.debug) {
                    System.out.println("Cache hit!");
                }
                return cacheData;
            }
            Object readObject = readSecureResponse(id, httpRequest);
            if (readObject != null) {
                if (BaseSettings.debug) {
                    System.out.println("Cache miss: " + "/v1/" + simpleName);
                }
                byte[] compressed = CompressionController.compress(gson.toJson(readObject).getBytes());
                genericLocalCAO.create(CacheTypesEnum.API, id, compressed);
                return compressed;
            }
        }
        return null;
    }

    @Override
    public List readPaginatedResponse(int page, int pageSize, boolean full, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("read", theClass.getSimpleName() + "-" + page + "_" + pageSize, userId, apiKey);
                loggingDAO.create(securityLog);
            }
            return genericDAO.readPaginated(pageSize, page, theClass, full);
        }
        return null;
    }

    @Override
    public byte[] readPaginatedAPIResponse(int page, int pageSize, boolean full, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("read", theClass.getSimpleName() + "-" + page + "_" + pageSize, userId, apiKey);
                loggingDAO.create(securityLog);
            }
            byte[] compressed = genericPageCAO.read("/v1/" + simpleName, page, pageSize);
            if (compressed != null) {
                if (BaseSettings.debug) {
                    System.out.println("Cache hit: " + "/v1/" + simpleName);
                }
                return compressed;
            }
            List readObjects = readPaginatedResponse(page, pageSize, full, httpRequest);
            if (readObjects != null) {
                if (BaseSettings.debug) {
                    System.out.println("Cache miss: " + "/v1/" + simpleName);
                }
                compressed = CompressionController.compress(gson.toJson(readObjects).getBytes());
                genericPageCAO.create("/v1/" + simpleName, page, pageSize, compressed);
                return compressed;
            }
        }
        return null;
    }

    @Override
    public boolean update(long id, Object object, HttpRequest httpRequest) throws Exception {
        if (canAccess(httpRequest, UPDATE_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("update", theClass.getSimpleName() + "-" + id, httpRequest)) {
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
            }
        }
        return false;
    }

    @Override
    public boolean delete(long id, HttpRequest httpRequest) throws Exception {
        if (canAccess(httpRequest, DELETE_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("delete", theClass.getSimpleName() + "-" + id, httpRequest)) {
                if (genericDAO.delete(id, theClass)) {
                    genericLocalCAO.delete(id);
                    genericPageCAO.delete();
                    genericSAO.deleteDocument(id, index);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List search(String query, String returnColumn, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, SEARCH_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("search", theClass.getSimpleName() + "-" + query, httpRequest)) {
                return genericDAO.databaseSearch(BaseSettings.maxSearchResults, query, returnColumn, SEARCH_COLUMN, theClass);
            }
        }
        return null;
    }

    @Override
    public byte[] searchAPIResponse(String query, String returnColumn, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, SEARCH_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("search", theClass.getSimpleName() + "-" + query, httpRequest)) {
                List searchList = search(query, returnColumn, httpRequest);
                if (searchList != null) {
                    return CompressionController.compress(gson.toJson(searchList).getBytes());
                }
            }
        }
        return null;
    }

    @Override
    public List readOrderByPaginated(int resultCount, int page, String orderBy, boolean ascending, boolean full, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog(orderBy, theClass.getSimpleName() + "-" + page + "_" + resultCount, httpRequest)) {
                return genericDAO.readOrderedBy(resultCount, page, theClass, orderBy, ascending, full);
            }
        }
        return null;
    }

    public List<String> readManyOrderByPaginated(int resultCount, int page, List<String> orderByList, List<Class> theClassList, boolean ascending, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog(orderByList.toString(), theClassList.toString() + "-" + page + "_" + resultCount, httpRequest)) {
                ResultSet data = genericDAO.readManyOrderedBy(resultCount, page, theClassList, orderByList, ascending);
                List<String> list = new LinkedList<>();
                while (data.next()) {
                    list.add(data.getLong("id") + "-" + data.getString("mediaType"));
                }
                return list;
            }
        }
        return null;
    }

    public List getFromReadMany(String orderBy, Class theClass, List<String> data, boolean full) throws SQLException {
        List<BasicFormObject> objects = new LinkedList<>();
        List<Long> missingIds = new LinkedList<>();
        for (String entry : data) {
            String[] splitEntry = entry.split("-");
            String key = splitEntry[0];
            String className = splitEntry[1];
            String orderByColumn = splitEntry[2];

            if (theClass.getSimpleName().toLowerCase().equals(className) && orderByColumn.equals(orderBy)) {
                long id = Long.parseLong(key);
                Object cachedObject = genericLocalCAO.read(CacheTypesEnum.DATABASE, id);
                if (cachedObject != null) {
                    objects.add((BasicFormObject) cachedObject);
                } else {
                    missingIds.add(id);
                }
            }
        }
        if (missingIds.size() > 0) {
            objects.addAll(genericDAO.readMany(missingIds, theClass, full));
        }
        return objects;
    }

    @Override
    public byte[] readOrderByPaginatedAPI(int resultCount, int page, String orderBy, boolean ascending, boolean full, HttpRequest httpRequest) throws IOException, SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("popular", theClass.getSimpleName() + "-" + page + "_" + resultCount, httpRequest)) {
                List readPage = readOrderByPaginated(resultCount, page, orderBy, ascending, full, httpRequest);
                if (readPage != null) {
                    return CompressionController.compress(gson.toJson(readPage).getBytes());
                }
            }
        }
        return null;
    }

    @Override
    public Long getTotal(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("total", theClass.getSimpleName(), httpRequest)) {
                return genericDAO.getTotal(theClass);
            }
        }
        return 0L;
    }

    @Override
    public Long getTotalPages(int maxResultsSize, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("totalPages", theClass.getSimpleName() + "-" + maxResultsSize, httpRequest)) {
                return genericDAO.getTotalPages(maxResultsSize, theClass);
            }
        }
        return 0L;
    }

    @Override
    public HttpResponse deleteAllIndexes(HttpRequest httpRequest) throws Exception {
        if (canAccess(httpRequest, PermissionGroupEnum.OPERATOR, basicUserController)) {
            if (createLog("deleteAllIndexes", theClass.getSimpleName(), httpRequest)) {
                genericSAO.deleteAllDocuments(index);
                genericSAO.reindex(REINDEX_BATCH_SIZE, genericDAO, index, theClass);
                return ok();
            }
        } else {
            return unauthorized();
        }
        return error();
    }

    @Override
    public boolean reindex(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, PermissionGroupEnum.OPERATOR, basicUserController)) {
            if (createLog("reindex", theClass.getSimpleName(), httpRequest)) {
                try {
                    genericSAO.reindex(BaseSettings.maxSearchResults, genericDAO, index, theClass);
                    return true;
                } catch (Exception e) {
                    if (BaseSettings.debug) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public byte[] getSample(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (canAccess(httpRequest, PermissionGroupEnum.USER, basicUserController)) {
            if (createLog("sample", theClass.getSimpleName(), httpRequest)) {
                return sample;
            }
        }
        return null;
    }

    public boolean createLog(String name, String value, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (BaseSettings.loggingEnabled) {
            Long userId = getUserId(httpRequest);
            String apiKey = getJwt(httpRequest);
            SecurityLog securityLog = new SecurityLog(name, value, userId, apiKey);
            if (BaseSettings.debug) {
                System.out.println("Adding to log: " + securityLog);
            }
            return loggingDAO.create(securityLog);
        } else {
            return true;
        }
    }
}
