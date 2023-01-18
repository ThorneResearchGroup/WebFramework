package tech.tresearchgroup.palila.controller.cache.local;

public interface BasicLocalPageCache {
    void create(String location, long page, long maxResults, byte[] data);

    byte[] read(String location, long page, long maxResults);

    void delete();
}
