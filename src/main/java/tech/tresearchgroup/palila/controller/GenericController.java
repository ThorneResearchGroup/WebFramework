package tech.tresearchgroup.palila.controller;

import com.google.gson.Gson;
import com.meilisearch.sdk.Client;
import com.zaxxer.hikari.HikariDataSource;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.serializer.BinarySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tresearchgroup.cao.model.CacheTypesEnum;
import tech.tresearchgroup.palila.model.*;
import tech.tresearchgroup.palila.model.enums.PermissionGroupEnum;
import tech.tresearchgroup.palila.model.enums.ReturnType;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class GenericController extends BaseController implements GenericControllerInterface {
    private static final Logger logger = LoggerFactory.getLogger(GenericController.class);
    private final PermissionGroupEnum CREATE_PERMISSION_LEVEL;
    private final PermissionGroupEnum READ_PERMISSION_LEVEL;
    private final PermissionGroupEnum UPDATE_PERMISSION_LEVEL;
    private final PermissionGroupEnum DELETE_PERMISSION_LEVEL;
    private final PermissionGroupEnum SEARCH_PERMISSION_LEVEL;
    private final BasicUserController basicUserController;
    private final Card cardTemplate;

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
                             BasicUserController basicUserController,
                             Card cardTemplate) throws Exception {
        super(hikariDataSource, gson, client, theClass, serializer, reindexSize, searchColumn, sample);
        this.CREATE_PERMISSION_LEVEL = createPermissionLevel;
        this.READ_PERMISSION_LEVEL = readPermissionLevel;
        this.UPDATE_PERMISSION_LEVEL = updatePermissionLevel;
        this.DELETE_PERMISSION_LEVEL = deletePermissionLevel;
        this.SEARCH_PERMISSION_LEVEL = searchPermissionLevel;
        this.basicUserController = basicUserController;
        this.cardTemplate = cardTemplate;
    }

    @Override
    public Object createSecureResponse(Object object, ReturnType returnType, HttpRequest httpRequest) throws Exception {
        if (canAccess(httpRequest, CREATE_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("create", theClass.getSimpleName(), userId, apiKey);
                loggingDAO.create(securityLog);
            }
            if (genericDAO.create(object)) {
                genericSAO.createDocument(object, index);
                genericCAO.delete(CacheTypesEnum.PAGE_API, theClass);
                genericCAO.delete(CacheTypesEnum.PAGE_DATABASE, theClass);
                if (returnType.equals(ReturnType.OBJECT)) {
                    return object;
                } else if (returnType.equals(ReturnType.JSON)) {
                    return CompressionController.compress(gson.toJson(object).getBytes());
                }
            }
        }
        return null;
    }

    @Override
    public Object readSecureResponse(long id, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("read", theClass.getSimpleName() + "-" + id, userId, apiKey);
                loggingDAO.create(securityLog);
            }
            byte[] cachedData = (byte[]) genericCAO.read(CacheTypesEnum.DATABASE, id);
            Object object;
            if (cachedData != null) {
                if (BaseSettings.debug) {
                    logger.info("Cache hit: " + "/v1/" + simpleName);
                }
                object = ActiveJSerializer.deserialize(cachedData, serializer);
            } else {
                if (BaseSettings.debug) {
                    logger.info("Cache miss: " + "/v1/" + simpleName);
                }
                object = genericDAO.read(id, theClass);
                if (BaseSettings.cacheEnable) {
                    if (object != null) {
                        byte[] binary = ActiveJSerializer.serialize(object, serializer);
                        genericCAO.create(CacheTypesEnum.DATABASE, id, binary);
                    }
                }
            }
            if (returnType.equals(ReturnType.OBJECT)) {
                return object;
            } else if (returnType.equals(ReturnType.JSON)) {
                byte[] compressed = CompressionController.compress(gson.toJson(object).getBytes());
                genericCAO.create(CacheTypesEnum.API, id, compressed);
                return compressed;
            }
        } else {
            return unauthorized();
        }
        return null;
    }

    @Override
    public Object readPaginatedResponse(int page, int pageSize, boolean full, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (BaseSettings.loggingEnabled) {
                Long userId = getUserId(httpRequest);
                String apiKey = getJwt(httpRequest);
                SecurityLog securityLog = new SecurityLog("read", theClass.getSimpleName() + "-" + page + "_" + pageSize, userId, apiKey);
                loggingDAO.create(securityLog);
            }
            Object object = genericDAO.readPaginated(pageSize, page, theClass, full);
            if (returnType.equals(ReturnType.OBJECT)) {
                return object;
            } else if (returnType.equals(ReturnType.JSON)) {
                ResultEntity resultEntity = new ResultEntity(theClass.getSimpleName(), (List) object);
                byte[] compressed = CompressionController.compress(gson.toJson(resultEntity).getBytes());
                genericCAO.create(CacheTypesEnum.PAGE_DATABASE, simpleName + "-" + page + "-" + pageSize, compressed);
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
                    genericCAO.update(CacheTypesEnum.DATABASE, id, binary);
                    byte[] compressed = CompressionController.compress(json);
                    genericCAO.update(CacheTypesEnum.API, id, compressed);
                    genericCAO.delete(CacheTypesEnum.PAGE_API, theClass);
                    genericCAO.delete(CacheTypesEnum.PAGE_DATABASE, theClass);
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
                    genericCAO.delete(CacheTypesEnum.DATABASE, id);
                    genericCAO.delete(CacheTypesEnum.PAGE_API, theClass);
                    genericCAO.delete(CacheTypesEnum.PAGE_DATABASE, theClass);
                    genericSAO.deleteDocument(id, index);
                    return true;
                } else if (BaseSettings.debug) {
                    logger.info("Failed to delete object");
                }
            } else if (BaseSettings.debug) {
                logger.info("Failed to create log");
            }
        } else if (BaseSettings.debug) {
            logger.info("Unauthorized access");
        }
        return false;
    }

    @Override
    public Object search(String query, String returnColumn, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        if (query != null && returnColumn != null) {
            if (query.length() > 0 && returnColumn.length() > 0) {
                if (canAccess(httpRequest, SEARCH_PERMISSION_LEVEL, basicUserController)) {
                    if (createLog("search", theClass.getSimpleName() + "-" + query, httpRequest)) {
                        Object object = genericDAO.search(BaseSettings.maxSearchResults, query, returnColumn, SEARCH_COLUMN, theClass);
                        if (returnType.equals(ReturnType.OBJECT)) {
                            return object;
                        } else if (returnType.equals(ReturnType.JSON)) {
                            return CompressionController.compress(gson.toJson(object).getBytes());
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Object readOrderByPaginated(int resultCount, int page, String orderBy, boolean ascending, boolean full, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog(orderBy, theClass.getSimpleName() + "-" + page + "_" + resultCount, httpRequest)) {
                Object object = genericDAO.readOrderedBy(resultCount, page, theClass, orderBy, ascending, full);
                if (returnType.equals(ReturnType.OBJECT)) {
                    return object;
                } else if (returnType.equals(ReturnType.JSON)) {
                    return CompressionController.compress(gson.toJson(object).getBytes());
                }
            }
        }
        return null;
    }

    public List<String> readManyOrderByPaginated(int resultCount, int page, List<String> orderByList, List<Class> theClassList, boolean ascending, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog(orderByList.toString(), theClassList.toString() + "-" + page + "_" + resultCount, httpRequest)) {
                ResultSet data = genericDAO.readManyOrderByPaginated(resultCount, page, theClassList, orderByList, ascending);
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
                Object cachedObject = genericCAO.read(CacheTypesEnum.DATABASE, id);
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
    public Long getTotal(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        if (canAccess(httpRequest, READ_PERMISSION_LEVEL, basicUserController)) {
            if (createLog("total", theClass.getSimpleName(), httpRequest)) {
                return genericDAO.getTotal(theClass);
            }
        }
        return 0L;
    }

    @Override
    public Long getTotalPages(int maxResultsSize, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
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
                return ok();
            }
        } else {
            return unauthorized();
        }
        return error();
    }

    @Override
    public boolean reindex(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        if (canAccess(httpRequest, PermissionGroupEnum.OPERATOR, basicUserController)) {
            if (createLog("reindex", theClass.getSimpleName(), httpRequest)) {
                try {
                    long totalPages = genericDAO.getTotalPages(BaseSettings.maxSearchResults, theClass);
                    for (int i = 0; i <= totalPages; i++) {
                        List items = genericDAO.readPaginated(BaseSettings.maxSearchResults, i, theClass, false);
                        genericSAO.reindex(REINDEX_BATCH_SIZE, items, index, theClass);
                    }
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
    public byte[] getSample(HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
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
                logger.info("Adding to log: " + securityLog);
            }
            return loggingDAO.create(securityLog);
        } else {
            return true;
        }
    }

    @Override
    public Card toCard(Object object, String action) throws InvocationTargetException, IllegalAccessException {
        Card outputCard = new Card();
        Class cardClass = cardTemplate.getClass();
        Field[] fields = cardClass.getDeclaredFields();
        outputCard.setAction(action);
        outputCard.setClassName(object.getClass().getSimpleName().toLowerCase());
        outputCard.setPosterLocation("/assets/poster.webp");
        for (Field field : fields) {
            if (field.getName().equals("id")) {
                Method idGetter = ReflectionMethods.getId(object.getClass());
                Long id = (Long) idGetter.invoke(object);
                outputCard.setId(id);
            } else if (field.getName().equals("posterLocation")) {
                //Todo implement image loading
            } else {
                Method varNameGetter = ReflectionMethods.getGetter(field, cardClass);
                String getterData = (String) varNameGetter.invoke(cardTemplate);
                if (getterData != null) {
                    String varName = getterData.substring(0, 1).toUpperCase() + getterData.substring(1);
                    try {
                        Method getter = object.getClass().getMethod("get" + varName);
                        String objectValue = String.valueOf(getter.invoke(object));
                        Method setter = ReflectionMethods.getSetter(field, outputCard.getClass(), String.class);
                        setter.invoke(outputCard, objectValue);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return outputCard;
    }
}
