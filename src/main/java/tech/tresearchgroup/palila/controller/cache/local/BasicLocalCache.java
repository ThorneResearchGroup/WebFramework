package tech.tresearchgroup.palila.controller.cache.local;

import tech.tresearchgroup.palila.model.enums.CacheTypesEnum;

public interface BasicLocalCache {
    void create(CacheTypesEnum cacheTypesEnum, long id, byte[] data);

    byte[] read(CacheTypesEnum cacheTypesEnum, long id);

    void update(CacheTypesEnum cacheTypesEnum, long id, byte[] data);

    void delete(long id);
}
