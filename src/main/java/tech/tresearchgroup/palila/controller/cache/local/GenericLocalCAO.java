package tech.tresearchgroup.palila.controller.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.enums.CacheTypesEnum;

public class GenericLocalCAO implements BasicLocalCache {
    private final Cache<Long, byte[]> apiCache;
    private final Cache<Long, byte[]> databaseCache;

    public GenericLocalCAO() {
        this.apiCache = Caffeine.newBuilder().maximumSize(BaseSettings.apiCacheSize).build();
        this.databaseCache = Caffeine.newBuilder().maximumSize(BaseSettings.databaseCacheSize).build();
    }

    @Override
    public void create(CacheTypesEnum cacheTypesEnum, long id, byte[] data) {
        if (BaseSettings.cacheEnable) {
            switch (cacheTypesEnum) {
                case API -> apiCache.put(id, data);
                case DATABASE -> databaseCache.put(id, data);
            }
        }
    }

    @Override
    public byte[] read(CacheTypesEnum cacheTypesEnum, long id) {
        if (BaseSettings.cacheEnable) {
            switch (cacheTypesEnum) {
                case API -> {
                    return apiCache.getIfPresent(id);
                }
                case DATABASE -> {
                    return databaseCache.getIfPresent(id);
                }
            }
        }
        return null;
    }

    @Override
    public void update(CacheTypesEnum cacheTypesEnum, long id, byte[] data) {
        if (BaseSettings.cacheEnable) {
            switch (cacheTypesEnum) {
                case API: {
                    apiCache.invalidate(id);
                    apiCache.put(id, data);
                }
                case DATABASE: {
                    databaseCache.invalidate(id);
                    databaseCache.put(id, data);
                }
            }
        }
    }

    @Override
    public void delete(long id) {
        if (BaseSettings.cacheEnable) {
            apiCache.invalidate(id);
            databaseCache.invalidate(id);
        }
    }
}
