package tech.tresearchgroup.palila.controller;

import io.activej.http.HttpHeaders;
import io.activej.http.HttpResponse;
import io.activej.inject.module.AbstractModule;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.CachedEntity;
import tech.tresearchgroup.palila.model.enums.CompressionMethodEnum;

import java.io.IOException;

public class HttpResponses extends AbstractModule {
    public HttpResponse ok() {
        return HttpResponse.ok200();
    }

    public HttpResponse ok(byte[] data) throws IOException {
        if (BaseSettings.maintenanceMode) {
            return redirect("/maintenance");
        }
        byte[] compressedData = CompressionController.compress(data);
        return okResponseCompressed(compressedData);
    }

    public HttpResponse ok(CachedEntity cachedEntity) {
        if (BaseSettings.maintenanceMode) {
            return redirect("/maintenance");
        }
        return okResponseCompressed(cachedEntity);
    }

    public HttpResponse ok(boolean returnThis) {
        if (returnThis) {
            return ok();
        }
        return error();
    }

    public HttpResponse okResponseCompressed(byte[] data) {
        if (BaseSettings.maintenanceMode) {
            return redirect("/maintenance");
        }
        if (BaseSettings.compressionMethod.equals(CompressionMethodEnum.NONE)) {
            return HttpResponse.ok200().withBody(data).withHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        }
        return HttpResponse
            .ok200()
            .withBody(data)
            .withHeader(HttpHeaders.CONTENT_ENCODING, BaseSettings.compressionMethod.toString())
            .withHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }

    public HttpResponse okResponseCompressed(CachedEntity cachedEntity) {
        if (BaseSettings.maintenanceMode) {
            return redirect("/maintenance");
        }
        if (BaseSettings.compressionMethod.equals(CompressionMethodEnum.NONE)) {
            return HttpResponse.ok200().withBody(cachedEntity.getData()).withHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        }
        return HttpResponse
            .ok200()
            .withBody(cachedEntity.getData())
            .withHeader(HttpHeaders.CONTENT_ENCODING, BaseSettings.compressionMethod.toString())
            .withHeader(HttpHeaders.LAST_MODIFIED, cachedEntity.getDate());
    }

    public HttpResponse okResponseCompressed(byte[] data, int cacheAge) {
        if (BaseSettings.maintenanceMode) {
            return redirect("/maintenance");
        }
        return HttpResponse
            .ok200()
            .withBody(data)
            .withHeader(HttpHeaders.CONTENT_ENCODING, BaseSettings.compressionMethod.toString())
            .withHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + cacheAge + ", immutable");
    }

    public HttpResponse redirect(String location) {
        return HttpResponse.redirect301(location).withHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }

    protected HttpResponse error() {
        return HttpResponse.ofCode(500);
    }

    protected HttpResponse error(Exception e) {
        if (BaseSettings.debug) {
            e.printStackTrace();
        }
        return error();
    }

    public HttpResponse accessDenied() {
        return redirect("/denied");
    }

    public HttpResponse unauthorized() {
        return HttpResponse.ofCode(401);
    }

    public HttpResponse notFound() {
        return HttpResponse.ofCode(404);
    }

    public HttpResponse notImplemented() {
        return HttpResponse.ofCode(501);
    }
}
