package tech.tresearchgroup.palila.controller.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class StaticCAO {
    private static final Cache<String, byte[]> staticPageCache = Caffeine.newBuilder().build();

    public static void create(String location, byte[] data) {
        staticPageCache.put(location, data);
    }

    public static byte[] read(String location) {
        return staticPageCache.getIfPresent(location);
    }

    public static void delete(String location) {
        staticPageCache.invalidate(location);
    }
}
