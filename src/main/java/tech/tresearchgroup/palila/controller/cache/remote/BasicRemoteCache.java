package tech.tresearchgroup.palila.controller.cache.remote;

import tech.tresearchgroup.palila.model.enums.CacheTypesEnum;

public interface BasicRemoteCache {
    void create(CacheTypesEnum cacheTypesEnum, String id, String data);

    String read(CacheTypesEnum cacheTypesEnum, String id);

    void update(CacheTypesEnum cacheTypesEnum, String id, String data);

    void delete(String id);
}
