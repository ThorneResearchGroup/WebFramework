package tech.tresearchgroup.palila.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.zaxxer.hikari.HikariDataSource;
import io.activej.serializer.BinarySerializer;
import tech.tresearchgroup.cao.controller.GenericCAO;
import tech.tresearchgroup.dao.controller.GenericDAO;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.SecurityLog;
import tech.tresearchgroup.sao.controller.GenericSAO;

public class BaseController extends BasicController {
    protected final GenericDAO genericDAO;
    protected final GenericCAO genericCAO;
    protected final GenericSAO genericSAO;
    protected final GenericDAO loggingDAO;
    protected final Gson gson;
    protected final Class theClass;
    protected final BinarySerializer serializer;
    protected final Index index;
    protected final int REINDEX_BATCH_SIZE;
    protected final String SEARCH_COLUMN;
    protected final byte[] sample;
    String simpleName;

    public BaseController(HikariDataSource hikariDataSource,
                          Gson gson,
                          Client client,
                          Class theClass,
                          BinarySerializer serializer,
                          int reindexSize,
                          String searchColumn,
                          Object sample) throws Exception {
        this.genericDAO = new GenericDAO(hikariDataSource, BaseSettings.databaseType, theClass);
        this.loggingDAO = new GenericDAO(hikariDataSource, BaseSettings.databaseType, SecurityLog.class);
        this.genericCAO = new GenericCAO(BaseSettings.cacheMethodEnum, BaseSettings.apiCacheSize, BaseSettings.databaseCacheSize, BaseSettings.apiCacheSize, BaseSettings.pageCacheSize, theClass);
        this.genericSAO = new GenericSAO(gson);
        this.gson = gson;
        this.theClass = theClass;
        this.simpleName = theClass.getSimpleName().toLowerCase();
        this.serializer = serializer;
        this.REINDEX_BATCH_SIZE = reindexSize;
        this.SEARCH_COLUMN = searchColumn;
        this.index = client.index(theClass.getSimpleName());
        this.sample = CompressionController.compress(new GsonBuilder().setPrettyPrinting().create().toJson(sample).getBytes());
    }
}
