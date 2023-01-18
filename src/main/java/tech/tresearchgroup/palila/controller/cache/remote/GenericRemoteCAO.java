package tech.tresearchgroup.palila.controller.cache.remote;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.enums.CacheTypesEnum;

public class GenericRemoteCAO implements BasicRemoteCache {
    private final RedisCommands<String, String> apiCache;
    private final RedisCommands<String, String> databaseCache;

    public GenericRemoteCAO(Class theClass) {
        RedisClient apiCacheClient = RedisClient.create("redis://localhost/" + theClass.getSimpleName() + "Api");
        StatefulRedisConnection<String, String> apiCacheConnection = apiCacheClient.connect();
        this.apiCache = apiCacheConnection.sync();
        RedisClient databaseCacheClient = RedisClient.create("redis://localhost/" + theClass.getSimpleName() + "Database");
        StatefulRedisConnection<String, String> databaseCacheConnection = databaseCacheClient.connect();
        this.databaseCache = databaseCacheConnection.sync();
    }

    @Override
    public void create(CacheTypesEnum cacheTypesEnum, String id, String data) {
        if (BaseSettings.cacheEnable) {
            switch (cacheTypesEnum) {
                case API -> apiCache.set(id, data);
                case DATABASE -> databaseCache.set(id, data);
            }
        }
    }

    @Override
    public String read(CacheTypesEnum cacheTypesEnum, String id) {
        if (BaseSettings.cacheEnable) {
            switch (cacheTypesEnum) {
                case API -> {
                    return apiCache.get(id);
                }
                case DATABASE -> {
                    return databaseCache.get(id);
                }
            }
        }
        return null;
    }

    @Override
    public void update(CacheTypesEnum cacheTypesEnum, String id, String data) {
        if (BaseSettings.cacheEnable) {
            switch (cacheTypesEnum) {
                case API: {
                    apiCache.del(id);
                    apiCache.set(id, data);
                }
                case DATABASE: {
                    databaseCache.del(id);
                    databaseCache.set(id, data);
                }
            }
        }
    }

    @Override
    public void delete(String id) {
        if (BaseSettings.cacheEnable) {
            apiCache.del(id);
            databaseCache.del(id);
        }
    }
}
