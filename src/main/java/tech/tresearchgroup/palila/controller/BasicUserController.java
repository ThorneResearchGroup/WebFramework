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
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.Card;
import tech.tresearchgroup.palila.model.enums.PermissionGroupEnum;
import tech.tresearchgroup.palila.model.enums.ReturnType;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

public class BasicUserController extends BaseController implements GenericControllerInterface {
    private static final Logger logger = LoggerFactory.getLogger(BasicUserController.class);
    private final Card cardTemplate;

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
                               PermissionGroupEnum searchPermissionLevel,
                               Card templateCard) throws Exception {
        super(hikariDataSource, gson, client, theClass, serializer, reindexSize, searchColumn, sample);
        this.cardTemplate = templateCard;
    }

    @Override
    public Object createSecureResponse(Object object, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        if (genericDAO.create(object)) {
            genericCAO.delete(CacheTypesEnum.PAGE_API, theClass);
            genericCAO.delete(CacheTypesEnum.PAGE_DATABASE, theClass);
            if (returnType.equals(ReturnType.OBJECT)) {
                return object;
            } else if (returnType.equals(ReturnType.JSON)) {
                return CompressionController.compress(gson.toJson(object).getBytes());
            }
        }
        return null;
    }

    @Override
    public Object readSecureResponse(long id, ReturnType returnType, HttpRequest httpRequest) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
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
            if (object != null) {
                if (BaseSettings.cacheEnable) {
                    if (BaseSettings.debug) {
                        logger.info("Adding to cache: " + object);
                    }
                    byte[] binary = ActiveJSerializer.serialize(object, serializer);
                    genericCAO.create(CacheTypesEnum.DATABASE, id, binary);
                }
            } else {
                if (BaseSettings.debug) {
                    logger.info("Failed to load: " + theClass.getSimpleName() + " : " + id);
                }
            }
        }
        if (returnType.equals(ReturnType.OBJECT)) {
            return object;
        } else if (returnType.equals(ReturnType.JSON)) {
            return CompressionController.compress(gson.toJson(object).getBytes());
        }
        logger.info("YES");
        return null;
    }

    @Override
    public Object readPaginatedResponse(int page, int pageSize, boolean full, ReturnType returnType, HttpRequest httpRequest) throws SQLException, IOException {
        Object object = genericDAO.readPaginated(pageSize, page, theClass, full);
        if (returnType.equals(ReturnType.OBJECT)) {
            return object;
        } else if (returnType.equals(ReturnType.JSON)) {
            return CompressionController.compress(gson.toJson(object).getBytes());
        }
        return null;
    }

    @Override
    public boolean update(long id, Object object, HttpRequest httpRequest) throws Exception {
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
        return false;
    }

    @Override
    public boolean delete(long id, HttpRequest httpRequest) throws Exception {
        if (genericDAO.delete(id, theClass)) {
            genericCAO.delete(CacheTypesEnum.DATABASE, id);
            genericCAO.delete(CacheTypesEnum.PAGE_API, theClass);
            genericCAO.delete(CacheTypesEnum.PAGE_DATABASE, theClass);
            genericSAO.deleteDocument(id, index);
            return true;
        }
        return false;
    }

    @Override
    public Object search(String query, String returnColumn, ReturnType returnType, HttpRequest httpRequest) throws SQLException, IOException {
        Object object = genericDAO.search(BaseSettings.maxSearchResults, query, returnColumn, SEARCH_COLUMN, theClass);
        if (returnType.equals(ReturnType.OBJECT)) {
            return object;
        } else if (returnType.equals(ReturnType.JSON)) {
            return CompressionController.compress(gson.toJson(object).getBytes());
        }
        return null;
    }

    @Override
    public Object readOrderByPaginated(int resultCount, int page, String orderBy, boolean ascending, boolean full, ReturnType returnType, HttpRequest httpRequest) {
        if (returnType.equals(ReturnType.OBJECT)) {
            return null;
        } else if (returnType.equals(ReturnType.JSON)) {
            return new byte[0];
        }
        return null;
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
            long totalPages = genericDAO.getTotalPages(BaseSettings.maxSearchResults, theClass);
            for (int i = 0; i <= totalPages; i++) {
                List items = genericDAO.readPaginated(BaseSettings.maxSearchResults, i, theClass, false);
                genericSAO.reindex(REINDEX_BATCH_SIZE, items, index, theClass);
            }
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
